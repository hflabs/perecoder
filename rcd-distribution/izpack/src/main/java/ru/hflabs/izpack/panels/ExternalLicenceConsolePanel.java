package ru.hflabs.izpack.panels;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.licence.AbstractLicenceConsolePanel;
import com.izforge.izpack.util.Console;

/**
 * Класс <class>ExternalLicenceConsolePanel</class> реализует панель отображения лицензии из внешнего файла
 *
 * @author Nazin Alexander
 */
public class ExternalLicenceConsolePanel extends AbstractLicenceConsolePanel {

    /** Текст лицензии */
    private String licenceText;

    public ExternalLicenceConsolePanel(Resources resources, PanelView<Console> panel) {
        super(panel, resources);
    }

    @Override
    public boolean run(InstallData installData, Console console) {
        licenceText = ExternalLicencePanel.loadLicence(installData);
        return super.run(installData, console);
    }

    @Override
    protected String getText() {
        return licenceText;
    }
}
