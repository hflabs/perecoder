package ru.hflabs.izpack.validator;

import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.panels.userinput.validator.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Класс <class>MD5Validator</class> реализует проверку контрольной суммы файла
 *
 * @author Nazin Alexander
 */
public class MD5Validator implements Validator {

    /** Название параметра */
    public static final String MD5_PARAMETER = "md5";
    /** Значение по умолчанию */
    private static final String DEFAULT_MD5 = "0";

    /**
     * Выполняет рассчет MD5 суммы для файла и сравнивает результат с ожидаемым
     *
     * @param expectedMD5 ожидаемая сумма
     * @param path целевой файл
     * @return Возвращает <code>TRUE</code>, если результаты совпадают
     */
    private boolean isEquals(String expectedMD5, Path path) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(path)) {
                byte[] buffer = new byte[4096];
                DigestInputStream dis = new DigestInputStream(is, md);
                dis.on(true);
                while (dis.read(buffer) != -1) {
                    // update digest
                }
            }
            // Формируем актуальную сумму
            String actualMD5 = new BigInteger(1, md.digest()).toString(16);
            // Проверяем, что суммы совпадают
            return expectedMD5.equals(actualMD5);
        } catch (NoSuchAlgorithmException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean validate(ProcessingClient client) {
        String pathToFile = client.getText();
        // Получаем файл
        Path path = Paths.get(pathToFile);
        // Проверяем, что файл существует
        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            return false;
        }
        // Получаем ожидаемую сумму
        String expectedMD5 = client.hasParams() ?
                client.getValidatorParams().get(MD5_PARAMETER) :
                DEFAULT_MD5;
        // Выполняем сравнение
        return isEquals(expectedMD5, path);
    }
}
