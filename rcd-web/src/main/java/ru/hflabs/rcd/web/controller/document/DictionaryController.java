package ru.hflabs.rcd.web.controller.document;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.google.common.net.HttpHeaders;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hflabs.rcd.Directories;
import ru.hflabs.rcd.backend.console.exports.ExportDictionaries;
import ru.hflabs.rcd.backend.console.exports.handlers.ExportDescriptor;
import ru.hflabs.rcd.backend.console.exports.handlers.dictionary.ExportDictionariesCommand;
import ru.hflabs.rcd.backend.console.imports.ImportDictionaries;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportDescriptor;
import ru.hflabs.rcd.backend.console.imports.handlers.dictionary.ImportDictionariesCommand;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.transfer.CommunicationException;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.service.IPagingService;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.PageRequestBean;
import ru.hflabs.rcd.web.model.PageResponseBean;
import ru.hflabs.rcd.web.model.document.DictionaryBean;
import ru.hflabs.rcd.web.model.transfer.DownloadDictionaryDescriptor;
import ru.hflabs.rcd.web.model.transfer.UploadDictionaryDescriptor;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.spring.Assert;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.net.MediaType.CSV_UTF_8;
import static com.google.common.net.MediaType.HTML_UTF_8;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.service.ServiceUtils.extractSingleDocument;
import static ru.hflabs.rcd.web.PagingUtils.findPageByCriteria;

/**
 * Класс <class>DictionaryController</class> реализует контроллер управления справочниками
 *
 * @see Dictionary
 */
@Controller(DictionaryController.MAPPING_URI + DictionaryController.NAME_POSTFIX)
@RequestMapping(DictionaryController.MAPPING_URI + DictionaryController.DATA_URI)
public class DictionaryController extends ControllerTemplate {

    public static final String MAPPING_URI = "dictionaries";

    /** Сервис работы с группами справочников */
    @Resource(name = "groupService")
    private IGroupService groupService;
    /** Сервис работы со справочниками */
    @Resource(name = "dictionaryService")
    private IDictionaryService dictionaryService;
    /** Сервис работы с МЕТА-полями справочника */
    @Resource(name = "metaFieldService")
    private IMetaFieldService metaFieldService;
    /** Сервис импорта справочников */
    @Resource(name = "importDictionaries")
    private ImportDictionaries importDictionaries;
    /** Сервис экспорта справочников */
    @Resource(name = "exportDictionaries")
    private ExportDictionaries exportDictionaries;

    /**
     * Возвращает модели загрузки и сохранения справочника
     *
     * @param modelDefinitionFactory фабрика построения моделей
     * @return Возвращает коллекцию моделей
     */
    public static Collection<ModelDefinition> createTransferModel(IServiceFactory<ModelDefinition, Class<?>> modelDefinitionFactory) {
        // Параметры доступа к файлу по умолчанию
        Map<String, Object> fileDefaultParameters = ImmutableMap.<String, Object>builder()
                .put(FilePreference.ENCODING, FilePreference.DEFAULT_ENCODING)
                .put(FilePreference.DELIMITER, FilePreference.DEFAULT_DELIMITER)
                .put(FilePreference.QUOTE, FilePreference.DEFAULT_QUOTE)
                .build();
        Map<String, Object> fileAvailableValues = ImmutableMap.<String, Object>builder()
                .put(FilePreference.ENCODING, IOUtils.availableEncodingNames(true))
                .build();
        // Дескриптор загрузки
        ModelDefinition uploadDictionaryDefinition = modelDefinitionFactory.retrieveService(UploadDictionaryDescriptor.class);
        {
            uploadDictionaryDefinition.setDefaultParameters(fileDefaultParameters);
            uploadDictionaryDefinition.setAvailableValues(fileAvailableValues);
        }
        // Дескриптор сохранения
        ModelDefinition downloadDictionaryDefinition = modelDefinitionFactory.retrieveService(DownloadDictionaryDescriptor.class);
        {
            downloadDictionaryDefinition.setDefaultParameters(fileDefaultParameters);
            downloadDictionaryDefinition.setAvailableValues(fileAvailableValues);
        }
        // Возвращаем модели загрузки и сохранения справочника
        return Arrays.asList(uploadDictionaryDefinition, downloadDictionaryDefinition);
    }

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    @ResponseBody
    public Collection<ModelDefinition> createModel() {
        return ImmutableList.<ModelDefinition>builder()
                .add(modelDefinitionFactory.retrieveService(Dictionary.class))
                .addAll(createTransferModel(modelDefinitionFactory))
                .build();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public PageResponseBean<DictionaryBean> getDictionaries(
            @RequestParam(value = Dictionary.GROUP_ID, required = false) final String groupId,
            @Valid @ModelAttribute final PageRequestBean page) {
        // Форматируем параметры
        final String targetGroupId = FormatUtil.parseString(groupId);
        // Выполняем поиск страницы справочников
        return findPageByCriteria(page.getPageSize(), defaultPagingSize, page.getPage(), new IPagingService<DictionaryBean>() {
            @Override
            public FilterResult<DictionaryBean> findPage(int count, int offset) {
                // Формируем критерий
                FilterCriteria filterCriteria = page.createFilterCriteria()
                        .injectOffset(offset)
                        .injectCount(count);
                // Устанавливаем группу
                if (targetGroupId != null) {
                    filterCriteria.injectFilters(
                            ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                    Dictionary.GROUP_ID, new FilterCriteriaValue.StringValue(targetGroupId)
                            )
                    );
                }
                // Выполняем поиск
                FilterResult<Dictionary> dictionaries = dictionaryService.findByCriteria(filterCriteria, true);
                // Формируем декораторы
                Collection<DictionaryBean> result = Lists.newArrayList(
                        Collections2.transform(dictionaries.getResult(), DictionaryBean.CONVERT)
                );
                // Возвращаем результат фильтрации
                return new FilterResult<>(result, dictionaries.getCountByFilter(), dictionaries.getTotalCount());
            }
        });
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DictionaryBean getDictionary(@PathVariable String id) {
        return DictionaryBean.CONVERT.apply(
                dictionaryService.findByID(id, true, false)
        );
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public DictionaryBean createDictionary(@RequestBody Dictionary dictionary) {
        // Выполняем создание справочника
        Dictionary result = createSingleDocument(dictionaryService, injectId(dictionary, null), true);
        // Выполняем создание первичного МЕТА-поля справочника
        MetaField metaField = new MetaField();
        {
            metaField = linkRelative(dictionary, metaField);
            metaField = injectName(metaField, MetaField.DEFAULT_NAME);
            metaField.setOrdinal(MetaField.DEFAULT_ORDINAL);
            metaField.establishFlags(MetaField.FLAG_PRIMARY);
        }
        createSingleDocument(metaFieldService, metaField, false);
        // Возвращаем созданный справочник
        return DictionaryBean.CONVERT.apply(result);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public DictionaryBean updateDictionary(@PathVariable String id, @RequestBody Dictionary dictionary) {
        return DictionaryBean.CONVERT.apply(
                updateSingleDocument(dictionaryService, injectId(dictionary, id), true)
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void closeDictionary(@PathVariable String id) {
        dictionaryService.closeByIDs(Sets.newHashSet(id));
    }

    @RequestMapping(value = "/download/{dictionaryId}", method = RequestMethod.GET)
    public void downloadDictionary(@PathVariable String dictionaryId, @Valid @ModelAttribute DownloadDictionaryDescriptor bean, HttpServletResponse response, Locale locale) throws Throwable {
        final File directory = new File(Directories.TMP_FOLDER.getLocation(), String.valueOf(System.nanoTime()));
        // Получаем целевой справочник
        final Dictionary targetDictionary = dictionaryService.findByID(dictionaryId, true, false);
        try {
            // Формируем параметры экспорта
            ExportDictionariesCommand command = bean.getDelegate();
            command.setGroupName(targetDictionary.getRelative().getName());
            command.setDictionaryName(targetDictionary.getName());
            command.setFileType(FilesByExtensionFilter.CSV);
            // Устанавливаем директорию экспорта
            command.setTargetPath(directory.getCanonicalPath());
            // Выполняем экспорт
            ExportDescriptor descriptor = exportDictionaries.exportDocuments(command);
            if (!descriptor.hasErrors()) {
                // Получаем файл справочника
                DictionaryNamedPath path = new DictionaryNamedPath(command.getGroupName(), command.getDictionaryName());
                File exportedFile = descriptor.getFiles().get(path);
                Assert.notNull(exportedFile, String.format("Can't find file for dictionary %s", path));
                // Форминуем название файла
                String fileName = decode(encode(exportedFile.getName(), Charsets.UTF_8.name()), Charsets.ISO_8859_1.name());
                // Формируем контент ответа
                response.setContentType(CSV_UTF_8.toString());
                response.setContentLength((int) exportedFile.length());
                // Форминуем заголовок ответа
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", fileName));
                // Выполняем копирование результирующего файла в поток вывода
                FileCopyUtils.copy(new FileInputStream(exportedFile), response.getOutputStream());
            }
        } catch (Throwable ex) {
            throw new CommunicationException(messageSource.getMessage("CommunicationException.downloadDictionaryDescriptor", null, locale));
        } finally {
            FileUtils.deleteQuietly(directory);
        }
    }

    /**
     * Выполняет трансфер импортируемого файла
     *
     * @param file файл импорта
     * @return Возвращает путь к временному файлу
     */
    private String doTransferFile(MultipartFile file) throws IOException {
        String prefix = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.nanoTime() + "_";
        String suffix = "." + FilenameUtils.getExtension(file.getOriginalFilename());
        File targetFile = File.createTempFile(prefix, suffix, Directories.TMP_FOLDER.getLocation());
        file.transferTo(targetFile);
        return targetFile.getCanonicalPath();
    }

    /**
     * Выполняет загрузку справочника
     *
     * @param command подготовленная команда
     * @param path именованный путь справочника
     * @param file целевой файл
     */
    private DictionaryBean doUploadDictionary(ImportDictionariesCommand command, DictionaryNamedPath path, String description, MultipartFile file, Locale locale) throws Exception {
        try {
            command.setGroupName(path.getGroupName());
            command.setDictionaryName(path.getDictionaryName());
            command.setDictionaryDescription(description);
            // Выполняем передачу целевого файла
            command.setTargetPath(doTransferFile(file));
            // Выполняем импорт справочника
            ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(command);
            // Проверяем ошибки импорта
            if (descriptor.hasErrors()) {
                throw new CommunicationException(messageSource.getMessage("CommunicationException.uploadDictionaryDescriptor", null, locale));
            }
            // Получаем загруженный справочник
            Group group = extractSingleDocument(descriptor.getDocuments());
            Dictionary dictionary = extractSingleDocument(group.getDescendants());
            // Формируем и возвращаем декоратор
            return DictionaryBean.CONVERT.apply(dictionary);
        } finally {
            FileUtils.deleteQuietly(command.retrieveTargetFile());
        }
    }

    /**
     * <b>Workaround для IE9+</b>
     * <p/>
     * Статус ответа должен быть всегда {@link HttpServletResponse#SC_OK OK} и Content-Type: text/html,
     * для того, чтобы браузер не предлагал сохранить возвращаемый контент
     */
    private void doUploadDictionary(BindingResult bindingResult, Callable<DictionaryBean> callback, HttpServletResponse response, Locale locale) throws Exception {
        Object result;
        if (bindingResult.hasErrors()) {
            result = doHandleValidationException(bindingResult, locale);
        } else {
            try {
                result = callback.call();
            } catch (IllegalPrimaryKeyException ex) {
                result = handleIllegalPrimaryKeyException(ex, locale);
            } catch (ApplicationException ex) {
                result = handleApplicationException(ex, locale);
            } catch (Throwable th) {
                result = handleThrowable(th, locale);
            }
        }
        response.setHeader(HttpHeaders.CONTENT_TYPE, HTML_UTF_8.toString());
        objectMapper.writeValue(response.getOutputStream(), result);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void uploadDictionary(
            @Valid @ModelAttribute final UploadDictionaryDescriptor bean,
            BindingResult bindingResult,
            @RequestPart final MultipartFile file,
            HttpServletResponse response,
            final Locale locale) throws Throwable {
        Callable<DictionaryBean> callback = new Callable<DictionaryBean>() {
            @Override
            public DictionaryBean call() throws Exception {
                Group targetGroup = groupService.findByID(bean.getGroupId(), true, false);
                DictionaryNamedPath path = new DictionaryNamedPath(targetGroup.getName(), bean.getName());
                return doUploadDictionary(bean.getDelegate(), path, bean.getDescription(), file, locale);
            }
        };
        doUploadDictionary(bindingResult, callback, response, locale);
    }

    @RequestMapping(value = "/upload/{dictionaryId}", method = RequestMethod.POST)
    public void uploadDictionary(
            @PathVariable final String dictionaryId,
            @Valid @ModelAttribute final UploadDictionaryDescriptor bean,
            BindingResult bindingResult,
            @RequestPart final MultipartFile file,
            HttpServletResponse response,
            final Locale locale) throws Throwable {
        Callable<DictionaryBean> callback = new Callable<DictionaryBean>() {
            @Override
            public DictionaryBean call() throws Exception {
                Dictionary targetDictionary = dictionaryService.findByID(dictionaryId, true, false);
                DictionaryNamedPath path = new DictionaryNamedPath(targetDictionary.getRelative().getName(), targetDictionary.getName());
                return doUploadDictionary(bean.getDelegate(), path, targetDictionary.getDescription(), file, locale);
            }
        };
        doUploadDictionary(bindingResult, callback, response, locale);
    }
}
