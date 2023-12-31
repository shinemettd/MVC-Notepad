import javax.swing.JToolBar;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JTextArea;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JColorChooser;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import javax.swing.JTabbedPane;
import javax.swing.JPanel;

import java.awt.Component;
import javax.swing.JViewport;

import javax.swing.JComponent;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import java.awt.Dimension;
import java.awt.Container;

import javax.swing.JList;
import javax.swing.JDialog;
import javax.swing.border.TitledBorder;

import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class Viewer {
    private JFileChooser fileChooser;
    private JFrame frame;
    private ActionController controller;
    private WindowController windowController;
    private TabsController tabsController;
    private FindDialogController findController;
    private MouseListener mouseController;
    private HelpMouseListener helpMouseController;
    private JTabbedPane tabPane;
    private Font contentFont;
    private Font submenuFont;
    private Font menuFont;
    private Font dialogFont;
    private JTextArea currentContent;
    private JMenu editMenu;
    private JMenuItem viewItemZoomIn;
    private JMenuItem viewItemZoomOut;
    private JMenuItem viewItemZoomDefault;
    private JCheckBoxMenuItem statusBarBox;
    private JCheckBox caseSensitiveButton;
    private Font fontZoom;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JTextField searchField;
    private JRadioButton upButton;
    private JRadioButton downButton;
    private JDialog goDialog;
    private JDialog findDialog;
    private JDialog fontDialog;
    private JDialog helpDialog;
    private boolean isLightTheme;
    private CustomThemeMaker currentTheme;

    public Viewer() {
        frame = getFrame();
        mouseController = new MouseListener();
        helpMouseController = new HelpMouseListener();
        tabsController = new TabsController(this);
        findController = new FindDialogController(this);
        controller = new ActionController(this, tabsController, findController);
        windowController = new WindowController(controller, this);
        contentFont = new Font("Arial", Font.PLAIN, 14);
        menuFont = new Font("Tahoma", Font.BOLD, 18);
        submenuFont = new Font("Tahoma", Font.PLAIN, 16);
        dialogFont = new Font("Tahoma", Font.PLAIN, 12);
        tabPane = new JTabbedPane();
        isLightTheme = true;
        currentTheme = new CustomThemeMaker(isLightTheme);
        fileChooser = new JFileChooser();
        FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        fileChooser.setFileFilter(fileNameExtensionFilter);
    }

    public void startApplication() {
        JMenuBar menuBar = getJMenuBar();
        JToolBar toolBar = getToolBar(controller);
        createNewTab();
        initStatusPanel();
        frame.setJMenuBar(menuBar);
        frame.add(toolBar, BorderLayout.NORTH);
        frame.add(statusPanel, BorderLayout.SOUTH);
        frame.add(tabPane);
        frame.addWindowListener(windowController);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);

        ImageIcon notepadIcon = new ImageIcon("images/notepad.png");
        frame.setIconImage(notepadIcon.getImage());
        changeTheme();
    }

    public int createNewTab() {
        BorderLayout panelBorder = new BorderLayout();
        JPanel panel = new JPanel(panelBorder);
        JTextArea content = new JTextArea();
        TextAreaListener textAreaListener = new TextAreaListener(content, editMenu);

        content.setFont(contentFont);
        content.addCaretListener(textAreaListener);

        Document document = content.getDocument();
        document.addDocumentListener(textAreaListener);
        document.addDocumentListener(tabsController);

        JScrollPane scrollPane = new JScrollPane(content);

        panel.add(scrollPane, BorderLayout.CENTER);
        tabPane.addTab(null, panel);
        int tabIndex = tabPane.indexOfComponent(panel);
        tabPane.setTabComponentAt(tabIndex, createCustomTabComponent("Untitled.txt"));
        tabPane.setBackgroundAt(tabIndex, currentTheme.getBackgroundColor());

        tabsController.getFilesPerTabs().add(tabIndex, null);
        tabsController.getUnsavedChangesPerTab().add(tabIndex, false);

        return tabIndex;
    }

    public JTabbedPane getTabPane() {
        return tabPane;
    }

    public int getCurrentTabIndex() {
        return tabPane.getSelectedIndex();
    }

    public void setCurrentContent() {
        JPanel currentPanel = getCurrentPanel();

        for (Component component : currentPanel.getComponents()) {
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                JViewport viewport = scrollPane.getViewport();
                if (viewport.getView() instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) viewport.getView();
                    CaretController caretController = new CaretController(this);
                    textArea.addCaretListener(caretController);
                    currentContent = textArea;
                    fontZoom = textArea.getFont();
                }
            }
        }
    }

    public JTextArea getCurrentContent() {
        return currentContent;
    }

    public Font getCurrentTextAreaFont() {
        return currentContent.getFont();
    }

    public void changeTheme() {
        isLightTheme = !isLightTheme;
        currentTheme = new CustomThemeMaker(isLightTheme);
        MetalLookAndFeel.setCurrentTheme(currentTheme);
        currentTheme.refreshTheme();
        updateMenuBarFontsColor();
        updateTabColors();
        updateFontDialogColors();
        updateHelpDialogColors();

        SwingUtilities.updateComponentTreeUI(tabPane);
        SwingUtilities.updateComponentTreeUI(fileChooser);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    public String getCurrentTextAreaContent() {
        return currentContent.getText();
    }

    public Color getCurrentTextAreaColor() {
        return currentContent.getForeground();
    }

    public void showDialogFinishPrintDocument() {
        JLabel coloredLabelText = new JLabel("The document has been printed.");
        coloredLabelText.setForeground(currentTheme.getTextColor());
        JOptionPane.showMessageDialog(frame, coloredLabelText, "Notepad MVC",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public List<String> getListTextFromTextAreaContent() {
        Document document = currentContent.getDocument();
        List<String> listTxt = new ArrayList<>();
        try {
            String txt = document.getText(0, document.getLength());
            listTxt.addAll(Arrays.asList(txt.split("\n")));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return listTxt;
    }

    public Color openColorChooser() {
        return JColorChooser.showDialog(frame, "Color Chooser", Color.BLACK);
    }

    public void openFindDialog() {
        findDialog = createDialog("Find", false, 480, 170);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        ImageIcon findIcon = new ImageIcon("images/find.png");
        JLabel label = new JLabel(findIcon);
        label.setBounds(5, 15, 50, 30);
        label.setFont(dialogFont);

        searchField = new JTextField();
        searchField.setBounds(60, 20, 270, 20);

        JLabel directionLabel = new JLabel("Direction");
        directionLabel.setBounds(215, 60, 70, 20);
        directionLabel.setFont(dialogFont);

        ButtonGroup direction = new ButtonGroup();
        upButton = createRadioButton("Up", false, 180, 85, 60, 20);
        downButton = createRadioButton("Down", true, 240, 85, 60, 20);
        direction.add(upButton);
        direction.add(downButton);

        caseSensitiveButton = new JCheckBox("Case sensitive");
        caseSensitiveButton.setBounds(30, 80, 100, 25);
        caseSensitiveButton.setFont(dialogFont);
        caseSensitiveButton.setFocusable(false);

        JButton findButton = createDialogButton("Find", "Find", 350, 20, 90, 25);
        findButton.setEnabled(false);
        findButton.addActionListener(findController);

        TextFieldListener textFieldListener = new TextFieldListener(searchField, findButton);
        searchField.getDocument().addDocumentListener(textFieldListener);

        JButton cancelButton = createDialogButton("Cancel", "Cancel", 350, 78, 90, 25);
        cancelButton.addActionListener(findController);

        panel.add(label);
        panel.add(searchField);
        panel.add(directionLabel);
        panel.add(upButton);
        panel.add(downButton);
        panel.add(findButton);
        panel.add(cancelButton);
        panel.add(caseSensitiveButton);

        findDialog.add(panel);
        findDialog.setVisible(true);
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JRadioButton getDownButton() {
        return downButton;
    }

    public JCheckBox getCaseSensitiveButton() {
        return caseSensitiveButton;
    }

    public void closeFindDialog() {
        findDialog.dispose();
    }

    public void openGoDialog() {
        goDialog = createDialog("Go to the line", true, 300, 150);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel label = new JLabel("The line number:");
        label.setBounds(15, 10, 200, 20);
        label.setFont(dialogFont);

        JTextField textField = new JTextField();
        textField.setBounds(15, 35, 250, 20);
        filterInput(textField);

        GoDialogController dialogController = new GoDialogController(this, textField);

        JButton goToButton = createDialogButton("Go", "Go", 70, 70, 90, 25);
        goToButton.addActionListener(dialogController);

        JButton cancelButton = createDialogButton("Cancel", "Cancel", 175, 70, 90, 25);
        cancelButton.addActionListener(dialogController);

        panel.add(label);
        panel.add(textField);
        panel.add(goToButton);
        panel.add(cancelButton);

        goDialog.add(panel);
        goDialog.setVisible(true);
    }

    public void closeGoDialog() {
        goDialog.dispose();
    }

    public void showError(String errorMessage) {
        JOptionPane.showMessageDialog(
                new JFrame(),
                errorMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public File getFile() {
        JFrame fileChooserFrame = new JFrame();
        int returnVal = fileChooser.showOpenDialog(fileChooserFrame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            return file;
        }

        return null;
    }

    public File getNewFileSaveLocation(String fileName) {
        if (!fileName.equals("Untitled.txt")) {
            File selectedFile = new File(fileName);
            fileChooser.setSelectedFile(selectedFile);
        }

        JFrame fileChooserFrame = new JFrame();
        int returnValue = fileChooser.showSaveDialog(fileChooserFrame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile;
        }

        return null;
    }

    public void update(String text, String tabName, int tabIndex) {
        tabPane.setSelectedIndex(tabIndex);
        updateText(text);
        renameTab(tabName, tabIndex);
    }

    public void updateText(String text) {
        setCurrentContent();
        currentContent.setText(text);
        currentContent.setCaretPosition(0);
    }

    public void updateTextColor(Color color) {
        currentContent.setForeground(color);
    }

    public JDialog getFontDialog() {
        return fontDialog;
    }

    public void openFontDialog() {
        if (fontDialog != null) {
            SwingUtilities.updateComponentTreeUI(fontDialog);
            fontDialog.setVisible(true);
            return;
        }

        FontController fontController = new FontController(this);
        ListSelectionController ListSelectionController = new ListSelectionController(this);

        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] styles = {"Regular", "Italic", "Bold", "Bold Italic"};
        Integer[] sizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};

        fontDialog = createDialog("Font", true, 500, 500);
        fontDialog.setLayout(null);
        fontDialog.setResizable(false);

        JLabel fontLabel = createLabel("Font:", 15, 10);
        String currentFont = currentContent.getFont().getFontName();
        JTextField fontTextField = createTextField("fontTextField", currentFont, 15, 27, 200, 30);
        JList<String> fontsList = new JList<>(fonts);
        fontsList.setSelectedValue(currentFont, true);
        fontsList.setName("FontsList");
        fontsList.addListSelectionListener(ListSelectionController);
        JScrollPane fontsScrollPane = new JScrollPane(fontsList);
        fontsScrollPane.setBounds(15, 57, 200, 150);

        JLabel styleLabel = createLabel("Style:", 235, 10);
        String currentStyle = getFontStyle(currentContent.getFont().getStyle());
        JTextField styleTextField = createTextField("styleTextField", currentStyle, 235, 27, 150, 30);
        JList<String> stylesList = new JList<>(styles);
        stylesList.setSelectedValue(currentStyle, true);
        stylesList.setName("StylesList");
        stylesList.addListSelectionListener(ListSelectionController);
        JScrollPane stylesScrollPane = new JScrollPane(stylesList);
        stylesScrollPane.setBounds(235, 57, 150, 150);

        JLabel sizeLabel = createLabel("Size:", 405, 10);
        int currentSize = currentContent.getFont().getSize();
        JTextField sizeTextField = createTextField("sizeTextField", String.valueOf(currentSize), 405, 27, 70, 30);
        JList<Integer> sizesList = new JList<>(sizes);
        sizesList.setSelectedValue(currentSize, true);
        sizesList.setName("SizesList");
        sizesList.addListSelectionListener(ListSelectionController);
        JScrollPane sizesScrollPane = new JScrollPane(sizesList);
        sizesScrollPane.setBounds(405, 57, 70, 150);

        JPanel panel = new JPanel();
        panel.setBounds(235, 217, 240, 100);
        BorderLayout panelBorder = new BorderLayout();
        panel.setLayout(panelBorder);
        panel.setName("samplePanel");
        TitledBorder border = BorderFactory.createTitledBorder("Sample");
        panel.setBorder(border);
        JLabel label = new JLabel("AaBbCc");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(currentContent.getFont());
        panel.add(label);

        JButton buttonOk = createDialogButton("Ok", "Ok", 260, 420, 100, 30);
        buttonOk.addActionListener(fontController);

        JButton buttonCancel = createDialogButton("Cancel", "Cancel", 380, 420, 100, 30);
        buttonCancel.addActionListener(fontController);

        fontDialog.add(buttonOk);
        fontDialog.add(buttonCancel);
        fontDialog.add(fontLabel);
        fontDialog.add(fontTextField);
        fontDialog.add(fontsScrollPane);
        fontDialog.add(styleLabel);
        fontDialog.add(stylesScrollPane);
        fontDialog.add(styleTextField);
        fontDialog.add(sizeLabel);
        fontDialog.add(sizeTextField);
        fontDialog.add(sizesScrollPane);
        fontDialog.add(panel);
        fontDialog.setVisible(true);
    }

    public void setNewFontForTextArea(Font font) {
        currentContent.setFont(font);
    }

    public void hideFontDialog() {
        fontDialog.setVisible(false);
    }

    public void zoomIn() {
        if (canZoomIn()) {
            fontZoom = new Font(currentContent.getFont().getFontName(), currentContent.getFont().getStyle(), currentContent.getFont().getSize() + 2);
            currentContent.setFont(fontZoom);
        }
    }

    public boolean canZoomIn() {
        if (fontZoom.getSize() > 48) {
            setViewItemZoomIn(false);
            return false;
        } else {
            setViewItemZoomIn(true);
            return true;
        }
    }

    public void setViewItemZoomIn(boolean active) {
        viewItemZoomIn.setEnabled(active);
    }

    public void zoomOut() {
        if (canZoomOut()) {
            int size = currentContent.getFont().getSize();
            size = Math.max(size - 2, 8);
            fontZoom = new Font(currentContent.getFont().getFontName(), currentContent.getFont().getStyle(), size);
            currentContent.setFont(fontZoom);
        }
    }

    public boolean canZoomOut() {
        if (fontZoom.getSize() <= 8) {
            setViewItemZoomOut(false);
            return false;
        } else {
            setViewItemZoomOut(true);
            return true;
        }
    }

    public void setViewItemZoomOut(boolean active) {
        viewItemZoomOut.setEnabled(active);
    }

    public void zoomDefault() {
        if (canZoomDefault()) {
            Font defaultFont = new Font(currentContent.getFont().getFontName(), currentContent.getFont().getStyle(), 22);
            currentContent.setFont(defaultFont);
        }
    }

    public boolean canZoomDefault() {
        return currentContent.getFont().getSize() >= 22 || currentContent.getFont().getSize() <= 22;
    }

    public JCheckBoxMenuItem getStatusBarBox() {
        return statusBarBox;
    }

    public void setLabelByTextAreaLines(int line, int column) {
        statusLabel.setText("  Line " + line + ", Column " + column);
    }

    public void setStatusPanelToVisible(boolean visible) {
        statusPanel.setVisible(visible);
    }

    public void openHelpDialog() {
        if (helpDialog != null) {
            SwingUtilities.updateComponentTreeUI(helpDialog);
            helpDialog.setVisible(true);
            return;
        }

        HelpController helpController = new HelpController(this);

        int x = frame.getX();
        int y = frame.getY();
        helpDialog = new JDialog(frame, "About Notepad", true);
        helpDialog.setSize(500, 250);
        helpDialog.setLocation(x + 150, y + 150);
        helpDialog.setLayout(null);
        helpDialog.setResizable(false);

        JLabel helpLabel = new JLabel("Notepad Template Method Design Pattern team");
        helpLabel.setBounds(90, 70, 300, 15);

        JLabel linkLabel = new JLabel("See the development process");
        linkLabel.setForeground(Color.BLUE);
        linkLabel.setBounds(90, 90, 300, 15);
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(helpMouseController);

        JButton buttonOk = new JButton("Ok");
        buttonOk.setBounds(370, 180, 100, 30);
        buttonOk.addActionListener(helpController);
        buttonOk.setActionCommand("Ok");

        helpDialog.add(helpLabel);
        helpDialog.add(linkLabel);
        helpDialog.add(buttonOk);

        helpDialog.setVisible(true);
    }

    public void hideHelpDialog() {
        helpDialog.setVisible(false);
    }

    public JButton getCloseBtnFromTab(int tabIndex) {
        Component tabComponent = tabPane.getTabComponentAt(tabIndex);

        if (tabComponent != null && tabComponent instanceof JComponent) {
            JComponent tabCustomComponent = (JComponent) tabComponent;
            Component closeButtonComponent = tabCustomComponent.getComponent(3);

            if (closeButtonComponent instanceof JButton) {
                JButton closeButton = (JButton) closeButtonComponent;
                return closeButton;
            }
        }

        return null;
    }

    public void removeDotInTab(int tabIndex) {
        JButton closeBtn = getCloseBtnFromTab(tabIndex);
        closeBtn.setText("\u00d7");
    }

    public void setDotInTab(int tabIndex) {
        JButton closeBtn = getCloseBtnFromTab(tabIndex);
        closeBtn.setText("\u2022");
    }

    public void closeCurrentTab(JButton closeBtn) {
        int foundIndex = findTabIndexByCloseButton(closeBtn);
        int currentTabIndex = foundIndex != -1 ? foundIndex : tabPane.getSelectedIndex();
        tabPane.setSelectedIndex(currentTabIndex);

        if (currentTabIndex > 0) {
            if (controller.hasUnsavedChanges(currentTabIndex)) {
                showCloseTabMessage(currentTabIndex);
            } else {
                deleteTab(currentTabIndex);
            }
        } else if (currentTabIndex == 0) {
            int tabCount = tabPane.getTabCount();

            if (tabCount != 1 && controller.hasUnsavedChanges(currentTabIndex)) {
                showCloseTabMessage(currentTabIndex);
            } else if (controller.hasUnsavedChanges(currentTabIndex)) {
                showExitMessage();
            } else if (tabCount > 1 && !controller.hasUnsavedChanges(currentTabIndex)) {
                deleteTab(currentTabIndex);
            } else {
                System.exit(0);
            }
        }
    }

    public int showCloseTabMessage(int currentTabIndex) {
        JLabel coloredLabelText = new JLabel("Do you want to save changes ? ");
        coloredLabelText.setForeground(currentTheme.getTextColor());
        int result = JOptionPane.showConfirmDialog(frame, coloredLabelText, "Notepad MVC",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        if (result == JOptionPane.YES_OPTION) {
            int saveResult = ((SaveDocumentActionHandler) controller.getActionHandlers().get("Save")).saveDocument();

            if (saveResult == -1) {
                return -1;
            }

            deleteTab(currentTabIndex);
        } else if (result == JOptionPane.NO_OPTION) {
            deleteTab(currentTabIndex);
        }

        return result;
    }

    public void showExitMessage() {
        int result = JOptionPane.showConfirmDialog(frame, "Do you want to save changes ? ", "Notepad MVC",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);

        if (result == JOptionPane.YES_OPTION) {
            int saveResult = ((SaveDocumentActionHandler) controller.getActionHandlers().get("Save")).saveDocument();

            if (saveResult == 0) {
                System.exit(0);
            }

        } else if (result == JOptionPane.NO_OPTION) {
            System.exit(0);
        } else if (result == JOptionPane.CANCEL_OPTION) {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }

    private void updateMenuBarFontsColor() {
        JMenuBar menuBar = frame.getJMenuBar();

        if (menuBar == null) {
            return;
        }

        int menuCount = menuBar.getMenuCount();

        for (int i = 0; i < menuCount; i++) {
            JMenu menu = menuBar.getMenu(i);
            menu.setForeground(currentTheme.getTextColor());
            int itemCount = menu.getItemCount();

            for (int j = 0; j < itemCount; j++) {
                JMenuItem menuItem = menu.getItem(j);

                if (menuItem == null) {
                    continue;
                }

                if (menuItem instanceof JMenu) {
                    JMenu extraMenu = (JMenu) menuItem;
                    int extraItemCount = extraMenu.getItemCount();
                    for (int k = 0; k < extraItemCount; k++) {
                        JMenuItem extraMenuItem = extraMenu.getItem(k);

                        if (extraMenuItem == null) {
                            continue;
                        }

                        extraMenuItem.setForeground(currentTheme.getTextColor());
                    }
                }

                menuItem.setForeground(currentTheme.getTextColor());
            }
        }

        if (statusLabel != null) {
            statusLabel.setForeground(currentTheme.getTextColor());
        }
    }

    private void updateTabColors() {
        if (tabPane == null) {
            return;
        }

        int tabCount = tabPane.getTabCount();

        for (int i = 0; i < tabCount; i++) {
            Component tabComponent = tabPane.getComponentAt(i);
            tabPane.setBackgroundAt(i, currentTheme.getBackgroundColor());

            if (!(tabComponent instanceof JPanel)) {
                continue;
            }

            JPanel inPanelContent = (JPanel) tabComponent;
            JPanel panelContent = (JPanel) tabPane.getTabComponentAt(i);
            inPanelContent.setBackground(currentTheme.getBackgroundColor());
            inPanelContent.setForeground(currentTheme.getBackgroundColor());

            for (Component component : inPanelContent.getComponents()) {
                if (!(component instanceof JScrollPane)) {
                    continue;
                }
                updateScrollTheme(component);
                JScrollPane scrollPane = (JScrollPane) component;
                JViewport viewport = scrollPane.getViewport();

                if (viewport.getView() instanceof JTextArea) {
                    JTextArea textArea = (JTextArea) viewport.getView();
                    textArea.setBackground(currentTheme.getBackgroundColor());
                    textArea.setForeground(currentTheme.getTextColor());
                    textArea.setSelectionColor(currentTheme.getAlternativeColor());
                    textArea.setSelectedTextColor(currentTheme.getTextColor());
                }
            }

            for (Component component : panelContent.getComponents()) {
                if (component instanceof JLabel) {
                    JLabel tab = (JLabel) component;
                    tab.setForeground(currentTheme.getTextColor());
                    SwingUtilities.updateComponentTreeUI(tab);
                }

                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    EmptyBorder emptyBorder = new EmptyBorder(0, 0, 0, 0);
                    button.setBorder(emptyBorder);
                    button.setForeground(currentTheme.getTextColor());
                }
            }
        }
    }

    private void updateFontDialogColors() {
        if (fontDialog == null) {
            return;
        }

        javax.swing.JRootPane rootPane = (javax.swing.JRootPane) fontDialog.getComponent(0); //our fontDialog have only one component as JRootPane
        javax.swing.JLayeredPane layeredPane = (javax.swing.JLayeredPane) rootPane.getComponent(1); //previous JRootPane have empty JPanel and JLayeredPane that contains all components
        JPanel mainDialogPanel = (JPanel) layeredPane.getComponent(0);

        for (Component panelComponent : mainDialogPanel.getComponents()) {

            if (panelComponent instanceof JLabel) {
                JLabel label = (JLabel) panelComponent;
                label.setForeground(currentTheme.getTextColor()); //here changes font color on top of JLists (as a Font: Style: Size:)
                continue;
            }

            if (panelComponent instanceof JScrollPane) {
                updateScrollTheme(panelComponent);
                continue;
            }

            if (panelComponent instanceof JPanel) {
                JPanel secondDialogPanel = (JPanel) panelComponent;
                secondDialogPanel.setBackground(currentTheme.getSecondBackgroundColor()); //here changes background color  of example font label
                JLabel label = (JLabel) secondDialogPanel.getComponent(0);
                label.setForeground(currentTheme.getTextColor()); //here changes font color of example font label
                continue;
            }

            panelComponent.setBackground(currentTheme.getBackgroundColor()); //here changes others elements (JButton and JTextField)
            panelComponent.setForeground(currentTheme.getTextColor());
          }
    }

    private void updateHelpDialogColors() {
        if (helpDialog == null) {
            return;
        }

        javax.swing.JRootPane rootPane = (javax.swing.JRootPane) helpDialog.getComponent(0); //our helpDialog have only one component as JRootPane
        javax.swing.JLayeredPane layeredPane = (javax.swing.JLayeredPane) rootPane.getComponent(1); //previous JRootPane have empty JPanel and JLayeredPane that contains all components
        JPanel mainDialogPanel = (JPanel) layeredPane.getComponent(0);

        for (Component panelComponent : mainDialogPanel.getComponents()) {

            if (!(panelComponent instanceof JLabel)) {
                panelComponent.setBackground(currentTheme.getBackgroundColor());
                panelComponent.setForeground(currentTheme.getTextColor());
                continue;
            }

            JLabel label = (JLabel) panelComponent;
            if (!label.getText().equals("See the development process")) {
              panelComponent.setForeground(currentTheme.getTextColor());
            }
        }
    }

    private void updateScrollTheme(Component component) {
        if (component == null) {
            return;
        }
        JScrollPane scrollPane = (JScrollPane) component;

        for (Component scrollPaneElement : scrollPane.getComponents()) {

            if (scrollPaneElement instanceof JViewport) {
                JViewport vp = (JViewport) scrollPaneElement;

                for (Component vpComponent : vp.getComponents()) {
                    vpComponent.setBackground(currentTheme.getBackgroundColor()); //here changes JList from fontChooser
                    vpComponent.setForeground(currentTheme.getTextColor());
                }

                continue;
            }

            scrollPaneElement.setBackground(currentTheme.getBackgroundColor()); //here changes scroll
            scrollPaneElement.setForeground(currentTheme.getTextColor());
        }
    }



    private int findTabIndexByCloseButton(JButton closeBtn) {
        Container tabPanel = closeBtn.getParent();

        if (tabPanel != null) {
            int tabIndex = tabPane.indexOfTabComponent(tabPanel);

            if (tabIndex != -1) {
                return tabIndex;
            }
        }

        return -1;
    }

    private void filterInput(JTextField textField) {
        PlainDocument doc = (PlainDocument) textField.getDocument();
        IntegerFilter filter = new IntegerFilter(this);
        doc.setDocumentFilter(filter);
    }

    private JRadioButton createRadioButton(String name, boolean isSelected, int x, int y, int width, int height) {
        JRadioButton radioButton = new JRadioButton(name, isSelected);
        radioButton.setBounds(x, y, width, height);
        radioButton.setFont(dialogFont);
        radioButton.setFocusable(false);
        return radioButton;
    }

    private JButton createDialogButton(String name, String command, int x, int y, int width, int height) {
        JButton button = new JButton(name);

        button.setBounds(x, y, width, height);
        button.setFont(dialogFont);
        button.setFocusable(false);
        button.setActionCommand(command);

        return button;
    }

    private JDialog createDialog(String title, boolean isModal, int width, int height) {
        JDialog dialog = new JDialog(frame, title, isModal);

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocation(frame.getX() + 150, frame.getY() + 150);
        dialog.setSize(width, height);
        dialog.setResizable(false);

        return dialog;
    }

    private JLabel createLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 50, 15);

        return label;
    }

    private JTextField createTextField(String name, String text, int x, int y, int width, int height) {
        JTextField textField = new JTextField(text);
        textField.setBounds(x, y, width, height);
        textField.setName(name);

        return textField;
    }

    private String getFontStyle(int style) {
        if (style == Font.PLAIN) {
            return "Regular";
        } else if (style == Font.ITALIC) {
            return "Italic";
        } else if (style == Font.BOLD) {
            return "Bold";
        } else {
            return "Bold Italic";
        }
    }

    public void deleteTab(int tabIndex) {
        tabPane.removeTabAt(tabIndex);
        tabsController.getUnsavedChangesPerTab().remove(tabIndex);
        tabsController.getFilesPerTabs().remove(tabIndex);
    }

    private JMenu getHelpMenu() {
        JMenuItem viewHelpDocument = createMenuItem("View Help", "images/font.gif", "View_Help");
        viewHelpDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));

        JMenuItem aboutDocument = createMenuItem("About", "images/font.gif", "About");
        aboutDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(viewHelpDocument);
        helpMenu.add(aboutDocument);
        helpMenu.setFont(menuFont);

        return helpMenu;
    }

    private void initStatusPanel() {
        statusPanel = new JPanel();

        BevelBorder bevelBorder = new BevelBorder(BevelBorder.LOWERED);
        statusPanel.setBorder(bevelBorder);

        Dimension dimension = new Dimension(frame.getWidth(), 20);
        statusPanel.setPreferredSize(dimension);

        BoxLayout boxLayout = new BoxLayout(statusPanel, BoxLayout.X_AXIS);
        statusPanel.setLayout(boxLayout);

        statusLabel = new JLabel();

        statusPanel.add(statusLabel);
        statusPanel.setVisible(false);
    }

    private JPanel getCurrentPanel() {
        int currentTabIndex = tabPane.getSelectedIndex();

        if (currentTabIndex != -1) {
            Component currentTab = tabPane.getComponentAt(currentTabIndex);

            if (currentTab instanceof JPanel) {
                JPanel panel = (JPanel) currentTab;
                return panel;
            }
        }

        return null;
    }

    private JComponent createCustomTabComponent(String tabTitle) {
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 0, 0);
        JPanel tabPanel = new JPanel(flowLayout);
        tabPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        tabPanel.setOpaque(false);

        Dimension dimension = new Dimension(10, 10);
        tabPanel.add(Box.createRigidArea(dimension));

        JLabel label = new JLabel(tabTitle);
        Font labelFont = new Font("Tahoma", Font.PLAIN, 14);
        label.setFont(labelFont);
        tabPanel.add(label);

        dimension = new Dimension(40, 10);
        tabPanel.add(Box.createRigidArea(dimension));

        JButton closeTabBtn = createCloseTabBtn();
        tabPanel.add(closeTabBtn);

        dimension = new Dimension(5, 5);
        tabPanel.add(Box.createRigidArea(dimension));

        return tabPanel;
    }

    private JButton createCloseTabBtn() {
        JButton closeButton = new JButton("\u00d7");
        closeButton.addMouseListener(mouseController);
        closeButton.setFont(submenuFont);
        closeButton.setBorder(null);
        closeButton.setContentAreaFilled(false);
        closeButton.setActionCommand("CloseTab");
        closeButton.addActionListener(controller);

        return closeButton;
    }

    private void renameTab(String tabName, int tabIndex) {
        Component tabComponent = tabPane.getTabComponentAt(tabIndex);

        if (tabComponent instanceof Container) {
            Component[] components = ((Container) tabComponent).getComponents();

            for (Component component : components) {
                if (component instanceof JLabel) {
                    JLabel tabLabel = (JLabel) component;
                    tabLabel.setText(tabName);
                    break;
                }
            }
        }
    }

    private JToolBar getToolBar(ActionController controller) {
        JToolBar toolBar = new JToolBar();

        JButton buttonNew = createButton("images/new-document.png", "New_Document", controller);
        JButton buttonOpen = createButton("images/open.png", "Open_Document", controller);
        JButton buttonSave = createButton("images/save.png", "Save", controller);
        JButton buttonCut = createButton("images/cut.png", "Cut", controller);
        JButton buttonCopy = createButton("images/copy.png", "Copy", controller);
        JButton buttonPaste = createButton("images/paste.png", "Paste", controller);
        JButton buttonColor = createButton("images/color.png", "Choose_Color", controller);
        JButton buttonChangeTheme = createButton("images/change-theme.png", "Change_Theme", controller);

        toolBar.add(buttonNew);
        toolBar.add(buttonOpen);
        toolBar.add(buttonSave);
        toolBar.addSeparator();
        toolBar.add(buttonCut);
        toolBar.add(buttonCopy);
        toolBar.add(buttonPaste);
        toolBar.add(buttonColor);
        toolBar.addSeparator();
        toolBar.add(buttonChangeTheme);
        toolBar.setFloatable(true);
        toolBar.setRollover(true);

        return toolBar;
    }

    private JButton createButton(String iconPath, String actionCommand, ActionController controller) {
        ImageIcon buttonIcon = new ImageIcon(iconPath);
        JButton button = new JButton(buttonIcon);

        button.addActionListener(controller);
        button.setActionCommand(actionCommand);
        button.setFocusable(false);

        return button;
    }

    private JMenuBar getJMenuBar() {
        JMenu fileMenu = getFileMenu();
        editMenu = getEditMenu();
        JMenu formatMenu = getFormatMenu();
        JMenu viewMenu = getViewMenu();
        JMenu helpMenu = getHelpMenu();

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JMenu getFileMenu() {

        JMenuItem newDocument = createMenuItem("New Document", "images/new-document.png", "New_Document");
        newDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

        JMenuItem openDocument = createMenuItem("Open Document", "images/open.png", "Open_Document");
        openDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

        JMenuItem saveDocument = createMenuItem("Save", "images/save.png", "Save");
        saveDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

        JMenuItem saveAsDocument = createMenuItem("Save As...", "images/save_as.png", "Save_As");

        JMenuItem printDocument = createMenuItem("Print", "images/print.png", "Print");
        printDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

        JMenuItem exitProgram = createMenuItem("Exit", "images/exit.png", "Exit");

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(newDocument);
        fileMenu.add(openDocument);
        fileMenu.add(saveDocument);
        fileMenu.add(saveAsDocument);
        fileMenu.addSeparator();
        fileMenu.add(printDocument);
        fileMenu.addSeparator();
        fileMenu.add(exitProgram);
        fileMenu.setFont(menuFont);

        return fileMenu;
    }

    private JMenu getEditMenu() {
        JMenuItem cutDocument = createMenuItem("Cut", "images/cut.png", "Cut");
        cutDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        cutDocument.setEnabled(false);

        JMenuItem copyDocument = createMenuItem("Copy", "images/copy.png", "Copy");
        copyDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        copyDocument.setEnabled(false);

        JMenuItem pasteDocument = createMenuItem("Paste", "images/paste.png", "Paste");
        pasteDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

        JMenuItem clearDocument = createMenuItem("Clear", "images/clear.png", "Clear");
        clearDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        clearDocument.setEnabled(false);

        JMenuItem findDocument = createMenuItem("Find", "images/find.png", "Find");
        findDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        findDocument.setEnabled(false);

        JMenuItem findNextDocument = createMenuItem("Find next", "images/next.png", "Find_Next");
        findNextDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
        findNextDocument.setEnabled(false);

        JMenuItem findPrevDocument = createMenuItem("Find previous", "images/previous.png", "Find_Prev");
        findPrevDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK));
        findPrevDocument
        .setEnabled(false);

        JMenuItem goDocument = createMenuItem("Go", "images/go.png", "Go");
        goDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));

        JMenuItem selectAllDocument = createMenuItem("Select all", "images/selectAll.png", "Select_All");
        selectAllDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

        JMenuItem timeAndDateDocument = createMenuItem("Time and date", "images/time.png", "Time_And_Date");
        timeAndDateDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK));

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(cutDocument);
        editMenu.add(copyDocument);
        editMenu.add(pasteDocument);
        editMenu.add(clearDocument);
        editMenu.addSeparator();
        editMenu.add(findDocument);
        editMenu.add(findNextDocument);
        editMenu.add(findPrevDocument);
        editMenu.add(goDocument);
        editMenu.addSeparator();
        editMenu.add(selectAllDocument);
        editMenu.add(timeAndDateDocument);
        editMenu.setFont(menuFont);

        return editMenu;
    }

    private JMenu getFormatMenu() {
        JMenuItem wordWrap = createMenuItem("Word wrap", "", "Word_Wrap");
        wordWrap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));

        JMenuItem fontDocument = createMenuItem("Font", "images/font.png", "Font");
        fontDocument.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));

        JMenu formatMenu = new JMenu("Format");
        formatMenu.add(wordWrap);
        formatMenu.addSeparator();
        formatMenu.add(fontDocument);
        formatMenu.setFont(menuFont);

        return formatMenu;
    }

    private JMenu getViewMenu() {

        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(menuFont);
        JMenu viewZoom = new JMenu("Zoom");
        viewZoom.setFont(submenuFont);

        viewItemZoomIn = createMenuItem("Zoom In", null,
                "ZOOM_IN");
        viewItemZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
        viewItemZoomIn.setEnabled(true);

        viewItemZoomOut = createMenuItem("Zoom Out", null,
                "ZOOM_OUT");
        viewItemZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
        viewItemZoomOut.setEnabled(true);

        viewItemZoomDefault = createMenuItem("Restore Default Zoom", null,
                "ZOOM_DEFAULT");
        viewItemZoomDefault.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, ActionEvent.CTRL_MASK));
        viewItemZoomDefault.setEnabled(true);

        statusBarBox = new JCheckBoxMenuItem("StatusBar");
        statusBarBox.setOpaque(false);
        statusBarBox.setFocusable(false);
        statusBarBox.setFont(submenuFont);

        Dimension dimension = new Dimension(100, 20);
        statusBarBox.setPreferredSize(dimension);
        statusBarBox.setSelected(false);

        statusBarBox.addActionListener(controller);
        viewMenu.add(viewZoom);
        viewMenu.addSeparator();
        viewZoom.add(viewItemZoomIn);
        viewZoom.add(viewItemZoomOut);
        viewZoom.addSeparator();
        viewZoom.add(viewItemZoomDefault);
        viewMenu.add(statusBarBox);

        return viewMenu;
    }

    private JMenuItem createMenuItem(String name, String pathToIcon, String actionCommand) {
        ImageIcon itemIcon = new ImageIcon(pathToIcon);
        JMenuItem menuItem = new JMenuItem(name, itemIcon);
        menuItem.addActionListener(controller);
        menuItem.setActionCommand(actionCommand);
        menuItem.setFont(submenuFont);

        return menuItem;
    }

    private JFrame getFrame() {
        JFrame frame = new JFrame("Notepad MVC");
        frame.setLocation(250, 100);
        frame.setSize(1000, 650);
        return frame;
    }
}
