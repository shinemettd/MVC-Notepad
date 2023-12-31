import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.nio.charset.StandardCharsets;
import javax.swing.JTabbedPane;
import java.nio.charset.UnmappableCharacterException;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class OpenDocumentActionHandler implements ActionHandler {
    private Viewer viewer;
    private TabsController tabsController;

    public OpenDocumentActionHandler(Viewer viewer, TabsController tabsController) {
        this.viewer = viewer;
        this.tabsController = tabsController;
    }

    @Override
    public void handleAction(String command, ActionEvent event) {
        File file = viewer.getFile();

        if(file != null) {
            JTabbedPane tabPane = viewer.getTabPane();
            int newTabIndex = viewer.createNewTab();
            tabsController.setValueInToList(tabsController.getFilesPerTabs(), newTabIndex, file);

            String filePath = file.getAbsolutePath();
            String contentText = readFile(filePath);
            String fileName = getFileNameFromPath(filePath);

            tabsController.setIsFileOpening(true);
            viewer.update(contentText, fileName, newTabIndex);
            tabsController.setValueInToList(tabsController.getUnsavedChangesPerTab(), newTabIndex, false);
            tabsController.setIsFileOpening(false);
        }
    }

    private String readFile(String filePath) {
        StringBuilder fileContent = new StringBuilder();

        try {
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            for (String line : lines) {
                fileContent.append(line).append("\n");
                line = null;
            }
        } catch (UnmappableCharacterException e) {
            viewer.showError(e.toString());
            System.out.println("Can't encode this type of symbols");
        } catch (IOException e) {
            viewer.showError(e.toString());
        }

        return fileContent.toString();
    }

    public String readCurrentFile() {
        int tabIndex = viewer.getCurrentTabIndex();
        File currentOpenFile = tabsController.getFilesPerTabs().get(tabIndex);
        return readFile(currentOpenFile.getAbsolutePath());
    }


    private String getFileNameFromPath(String path) {
        String[] directories = path.split("\\\\");
        return directories[directories.length - 1];
    }
}
