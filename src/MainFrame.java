import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MainFrame extends JFrame{

    private JPanel MainPanel;
    private JButton CreateNew;
    private JPanel ButtomPanel;
    private JButton OpenFile;
    private JButton SaveFile;
    private JButton DeleteFile;
    private JTextArea textArea1;

    private JPanel CustomizePane;
    private JButton ClearB;
    private JLabel Size;
    private JButton dec;
    private JButton inc;
    private File currentFile = null;
    private String lastState;
    private final StackLinkedList<String> undoStack = new StackLinkedList<>(100);
    private final StackLinkedList<String> redoStack = new StackLinkedList<>(100);
    private boolean isChangingProgrammatically = false;
    private int defaultFontSize=20;

    private boolean isUndoing = false;
    private boolean isRedoing = false;

    public MainFrame() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {



        textArea1.setMargin(new Insets(10, 10, 10, 10)); //Add Padding
        textArea1.setFont(new Font("Arial", Font.PLAIN, 20));  // 18 = new font size
        //textArea1.setText("Please Create or Open a new File Before writing your Text!\nAny Text Entered Here without opening or creating a new file cannot be saved\nand it will be delete if you create a new file");
        setContentPane(MainPanel);
        setTitle("Text Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000,500);
        setMinimumSize(new Dimension(500,300));
        setVisible(true);
        setLocationRelativeTo(null); //This is to make sure the window opens in the center.
        setResizable(true);
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        textArea1.setLineWrap(true); // Disable line wrapping for horizontal scrolling

        inc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultFontSize++;
                textArea1.setFont(new Font("Arial", Font.PLAIN, defaultFontSize));
            }
        });
        dec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultFontSize--;
                textArea1.setFont(new Font("Arial", Font.PLAIN, defaultFontSize));
            }
        });
        ClearB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int ans = JOptionPane.showConfirmDialog(MainFrame.this,"Are you sure? This Cannot be Undone","You sure?",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(ans != JOptionPane.YES_OPTION){
                    return;
                }
                redoStack.flush();
                textArea1.setText("");
                undoStack.flush();
            }
        });
//        CreateNew.addActionListener(e -> System.out.println("ActionListener: Button clicked"));
//        CreateNew.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                System.out.println("Mouse entered button");
//            }
//        });
//        CreateNew.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                System.out.println("Key pressed while button focused");
//            }
//        });

        CreateNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirmation = JOptionPane.showConfirmDialog(MainFrame.this,"Have you saved your previous File? Make sure to save your previous file."
                ,"Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
                if(confirmation!=JOptionPane.YES_OPTION){
                    return;
                }
                redoStack.flush();
                setTitle("Text Editor");
                currentFile = null;
                textArea1.setText("");
                undoStack.flush();
                isRedoing = false;
                isUndoing = false;
            }
        });
        OpenFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(MainFrame.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                    currentFile = fileChooser.getSelectedFile();
                    try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        setTitle(currentFile.getName());
                        textArea1.setText(content.toString());
                        undoStack.flush();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        DeleteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(); // default directory
                int option = fileChooser.showOpenDialog(MainFrame.this); // select file to delete

                if (option == JFileChooser.APPROVE_OPTION) {
                    File fileToDelete = fileChooser.getSelectedFile();

                    int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                            "Are you sure you want to delete '" + fileToDelete.getName() + "'?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (fileToDelete.delete()) {
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "File '" + fileToDelete.getName() + "' deleted successfully!");
                            // Clear editor if the deleted file was open
                            if (currentFile != null && currentFile.equals(fileToDelete)) {
                                textArea1.setText("");
                                undoStack.flush();
                                currentFile = null;
                                setTitle("Text Editor");
                            }
                        } else {
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Failed to delete the file.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        SaveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentFile != null) {
                    try (FileWriter writer = new FileWriter(currentFile)) {
                        writer.write(textArea1.getText());
                        JOptionPane.showMessageDialog(MainFrame.this, "File saved successfully!");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                   // JOptionPane.showMessageDialog(MainFrame.this,
                   //         "No file selected. Please create a new file first.", "Error", JOptionPane.ERROR_MESSAGE);

                    int createnew = JOptionPane.showConfirmDialog(MainFrame.this,
                            "File does not Exist, Do you want to create a new file?","Create new File",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if(createnew != JOptionPane.YES_OPTION){
                        return;
                    }
                    String newFileName = JOptionPane.showInputDialog(MainFrame.this,"Enter File Name","Create new File",JOptionPane.INFORMATION_MESSAGE);
                    if(!newFileName.isEmpty() || !newFileName.trim().isEmpty()){
                        if(!newFileName.endsWith(".txt")){
                            newFileName += ".txt";
                        }
                        currentFile = new File(newFileName);
                        try {
                            if(currentFile.createNewFile()){
                                try (FileWriter writer = new FileWriter(currentFile)) {
                                    writer.write(textArea1.getText());
                                    JOptionPane.showMessageDialog(MainFrame.this, "File saved successfully!");
                                    setTitle(currentFile.getName());
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(MainFrame.this,
                                            "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                            else{
                                JOptionPane.showMessageDialog(MainFrame.this,"File Already Exist with this name","Error",JOptionPane.ERROR_MESSAGE);
                                currentFile = null;
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    else {
                        setTitle("Text Editor");
                        JOptionPane.showMessageDialog(MainFrame.this,"File Name Empty","Error",JOptionPane.ERROR_MESSAGE);
                        currentFile = null;
                    }

                }
            }
        });

        textArea1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            private String lastState = textArea1.getText();

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                saveState();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                saveState();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e){

            }
            private void saveState() {


                if (!isUndoing && !isRedoing) {
                    System.out.println("Value Entered In the UndoStack (No Undo) Entering Text previous state: "  + lastState); // For Debugging
                    undoStack.push(lastState);
                }
                lastState = textArea1.getText();
            }
        });

// Ctrl + Z binding
        textArea1.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        textArea1.getActionMap().put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!undoStack.isEmpty()) {

                    isUndoing = true;
                    String currentState = textArea1.getText();
                    String prevState = undoStack.pop();
                    textArea1.setText(prevState);
                    lastState = prevState; // update lastState to avoid double push

                    System.out.println("Value Entered In the RedoStack: "  + currentState); // For Debugging
                    redoStack.push(currentState);
                    isUndoing = false;
                }
            }
        });
        textArea1.getInputMap().put(KeyStroke.getKeyStroke("control shift Z"),"Redo");
        textArea1.getActionMap().put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRedoing = true;


                // This is in try catch block because when we undo and spam redo(ctrl shift z) the redo stacks become empty
                //and it throws a exception and the next code stops which means the isRedoing = false is never reached
                //which make the new text to not be entered in the undo stack
                try {
                    String beforeUndo = redoStack.pop(); //Getting the last state in the start to make sure the current state doesnt
                                                         //get pushed into undo stack and it just returns.


                    String currentState = textArea1.getText();
                    undoStack.push(currentState);
                    System.out.println("Redo: Entered In undo Stack: " + currentState);
                    //-
                    System.out.println("Value Popping Out of the RedoStack: " + beforeUndo); // For Debugging
                    isChangingProgrammatically = true;
                    textArea1.setText(beforeUndo);
                    isChangingProgrammatically = false;
                    System.out.println("Redo Ended");
                    isRedoing = false;
                } catch (RuntimeException ex) {
                    isRedoing = false;
                    throw new RuntimeException(ex);

                }


            }
        }) ;

    }

    public static void main(String[] args) {
        try {
            new MainFrame();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
