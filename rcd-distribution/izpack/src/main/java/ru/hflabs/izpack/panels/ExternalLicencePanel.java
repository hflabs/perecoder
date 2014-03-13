package ru.hflabs.izpack.panels;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Класс <class>ExternalLicencePanel</class> реализует UI панель отображения лицензии из внешнего файла
 *
 * @author Nazin Alexander
 */
public class ExternalLicencePanel extends IzPanel implements ActionListener {

    private static final long serialVersionUID = 3691043187997552948L;

    public static final String LICENCE_PATH = "izpack.licence.path";
    /** Licence aria */
    private JTextArea textArea;
    /** The radio buttons. */
    private JRadioButton yesRadio;
    private JRadioButton noRadio;

    /**
     * Constructs a <tt>LicencePanel</tt>.
     *
     * @param panel the panel meta-data
     * @param parent the parent window
     * @param installData the installation data
     * @param resources the resources
     * @param log the log
     */
    public ExternalLicencePanel(Panel panel,
                                InstallerFrame parent,
                                GUIInstallData installData,
                                Resources resources,
                                Log log) {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        buildUI();
    }

    /** Выполняет построение UI панели */
    private void buildUI() {
        add(LabelFactory.create(getString("LicencePanel.info"), parent.getIcons().get("history"), LEADING), NEXT_LINE);
        textArea = new JTextArea();
        {
            textArea.setName(GuiId.LICENCE_TEXT_AREA.id);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            // register a listener to trigger the default button if enter is pressed whilst the text area has the focus
            ActionListener fireDefault = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton defaultButton = parent.getRootPane().getDefaultButton();
                    if (defaultButton != null && defaultButton.isEnabled()) {
                        defaultButton.doClick();
                    }
                }
            };
            textArea.registerKeyboardAction(fireDefault, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        {
            scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        }
        add(scrollPane, NEXT_LINE);

        ButtonGroup group = new ButtonGroup();
        // Agree
        {
            yesRadio = new JRadioButton(getString("LicencePanel.agree"), false);
            yesRadio.setName(GuiId.LICENCE_YES_RADIO.id);
            group.add(yesRadio);
            add(yesRadio, NEXT_LINE);
            yesRadio.addActionListener(this);
        }
        // Not agree
        {
            noRadio = new JRadioButton(getString("LicencePanel.notagree"), true);
            noRadio.setName(GuiId.LICENCE_NO_RADIO.id);
            group.add(noRadio);
            add(noRadio, NEXT_LINE);
            noRadio.addActionListener(this);
            setInitialFocus(noRadio);
        }
        getLayoutHelper().completeLayout();
    }

    protected static String loadLicence(InstallData installData) {
        String licenceVariable = installData.getVariable(LICENCE_PATH);
        if (licenceVariable != null) {
            Path licencePath = Paths.get(licenceVariable);
            if (Files.exists(licencePath) && Files.isReadable(licencePath) && Files.isRegularFile(licencePath)) {
                try {
                    return new Scanner(licencePath, "UTF-8").useDelimiter("\\Z").next();
                } catch (IOException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (yesRadio.isSelected()) {
            parent.unlockNextButton();
        } else {
            parent.lockNextButton();
        }
    }

    @Override
    public boolean isValidated() {
        if (noRadio.isSelected()) {
            parent.exit();
            return false;
        }
        return yesRadio.isSelected();
    }

    @Override
    public void panelActivate() {
        String licence = loadLicence(installData);
        if (licence != null && licence.length() != 0) {
            textArea.setText(licence);
            textArea.setCaretPosition(0);
            if (!yesRadio.isSelected()) {
                parent.lockNextButton();
            }
        } else {
            parent.skipPanel();
        }
    }
}
