// HTMLViewer - a visible component that displays a HTML in a JFrame.

/* NoticeStart

CDSS Common Java Library
CDSS Common Java Library is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1994-2023 Colorado Department of Natural Resources

CDSS Common Java Library is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CDSS Common Java Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CDSS Common Java Library.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package RTi.Util.IO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleFileFilter;
import RTi.Util.Message.Message;

/**
 * A visible component that displays a HTML in a JFrame.
 * Scrollbars are automatically displayed as needed
 */
@SuppressWarnings("serial")
public class HTMLViewer extends JFrame implements ActionListener {

  class HTMLViewerAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent event) {
      setVisible(false); // Hide the Frame.
      dispose();
    }
  }

  /**
   * Initial window height.
   */
  public static final int HEIGHT = 400;

  /**
   * Initial window width.
   */
  public static final int WIDTH = 500;

  /**
   * Button to close the window.
   */
  private JButton _buttonClose;

  /**
   * Button to print the content.
   */
  private JButton _buttonPrint;

  /**
   * Button to save the content.
   */
  private JButton _buttonSave;

  /**
   * EditorPane to contain scrollable HTML.
   */
  private JEditorPane _textArea;

  /**
   * Create an HTML viewer JFrame centered on the first screen.
   */
  public HTMLViewer() {
    // setTitle(filename);

    initGUI();

    pack();
    JGUIUtil.center(this);
  }

  /**
   * Create an HTML viewer JFrame centered on the screen for the given component,
   * typically the calling application.
   */
  public HTMLViewer ( Component component ) {
    // setTitle(filename);

    initGUI();

    pack();
    JGUIUtil.center(this,component);
  }

  /**
   * Displays a file in an HTML viewer, centered on the first screen.
   *
   * @param filename the name of the file to display
   */
  HTMLViewer(String filename) {
    setTitle(filename);
    initGUI();

    pack();
    JGUIUtil.center(this);
    setVisible(true);
  }

  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();

    if (source == _buttonClose) {
        setVisible(false);
        dispose();
    }
    else if (source == _buttonPrint) {
        DocumentRenderer renderer = new DocumentRenderer();
        renderer.print(_textArea);
    }
    else if (source == _buttonSave) {
        saveToFile();
    }
  }

  /**
   * Initialize GUI
   */
  private void initGUI() {
    JGUIUtil.setIcon(this, JGUIUtil.getIconImage());
    setBackground(Color.lightGray);

    JPanel outerPNL = new JPanel();
    outerPNL.setLayout(new BorderLayout());
    getContentPane().add(outerPNL);

    TitledBorder border = new TitledBorder("");
    outerPNL.setBorder(border);

    _textArea = new JEditorPane();
    _textArea.setContentType("text/html");
    _textArea.setEditable(false);

    _textArea.setBorder(new EmptyBorder(2, 2, 2, 2));

    // Create the textArea.
    JScrollPane scrollPane = new JScrollPane(_textArea);
    scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    outerPNL.add(scrollPane, BorderLayout.CENTER);

    // Create buttons.
    JPanel southPNL = new JPanel();
    southPNL.setLayout(new BorderLayout());
    outerPNL.add(southPNL, BorderLayout.SOUTH);

    JPanel controlPNL = new JPanel();
    southPNL.add(controlPNL, BorderLayout.CENTER);

    _buttonPrint = new JButton("Print");
    _buttonPrint.addActionListener(this);
    controlPNL.add(_buttonPrint);

    _buttonSave = new JButton("Save");
    _buttonSave.addActionListener(this);
    controlPNL.add(_buttonSave);

    _buttonClose = new JButton("Close");
    _buttonClose.addActionListener(this);
    controlPNL.add(_buttonClose);

    this.addWindowListener(new HTMLViewerAdapter());
  }

  /**
   * Determines if specified file is write-able.
   * <p>
   * A file is write-able if it:
   * <ul>
   * <li> exists
   * <li> is a file (not a directory)
   * <li> user has write permission
   * <ul>
   *
   * @param selectedFile
   * @return true, if file is write-able, otherwise false
   */
  public boolean isWriteable(File selectedFile) {

    if (selectedFile != null && (selectedFile.canWrite() == false || selectedFile.isFile() == false)) {
        // Post an error message and return.
        JOptionPane.showMessageDialog(this, "You do not have permission to write to file:" + "\n" + selectedFile.getPath(), "File not Writable",
            JOptionPane.ERROR_MESSAGE);

        return false;
    }
    else {
      return true;
    }
  }

  /**
   * Read file into pane.
   *
   * @param filename name of the file to be displayed
   * @param pane JTextComponent to receive the text
   */
  /* TODO SAM Evaluate whether needed
  private void readFile(String filename, JTextComponent textpane) {
    try {
        FileReader fr = new FileReader(filename);
        textpane.read(fr, null);
        fr.close();
    }
    catch (IOException e) {
        System.err.println(e);
    }
  }
  */

  /**
   * Saves the contents of the HTMLViewer to a file.
   * <p>
   * The user will be prompted where to save the file with a JFileChooser.
   * The JFileChooser will open at the last directory used.
   */
  public void saveToFile() {
    JGUIUtil.setWaitCursor(this, true);

    JFileChooser jfc = JFileChooserFactory.createJFileChooser(JGUIUtil.getLastFileDialogDirectory());
    jfc.setDialogTitle("Save Command Status Report to File");
    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
    jfc.setSelectedFile(new File(JGUIUtil.getLastFileDialogDirectory()+File.separator + "CommandStatusReport.html"));
    SimpleFileFilter htmlFilter = new SimpleFileFilter("html", "HyperText Markup Language");
    jfc.addChoosableFileFilter(htmlFilter);
    jfc.setAcceptAllFileFilterUsed(false);

    JGUIUtil.setWaitCursor(this, false);

    while (true) {
        int retVal = jfc.showSaveDialog(this);

        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        else {
            File file = jfc.getSelectedFile();

            if (file.exists()) {
                String msg = "File " + file.toString() + " already exists!\n Do you want to overwrite it ?";
                int result = JOptionPane.showConfirmDialog(null, msg, "File exists", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    // Attempt to clear selected file, known not to work in 1.42.
                    jfc.setSelectedFile(null);
                    continue;
                }
                else {
                    if (isWriteable(file)) {
                        String currDir = (jfc.getCurrentDirectory()).toString();
                        JGUIUtil.setLastFileDialogDirectory(currDir);

                        writeFile(file, _textArea);
                        return;
                    }
               }
            }
            else {
                if (jfc.getCurrentDirectory().canWrite()) {
                    String currDir = (jfc.getCurrentDirectory()).toString();
                    JGUIUtil.setLastFileDialogDirectory(currDir);

                    writeFile(file, _textArea);
                    return;
                }
                else {
                    String msg = (jfc.getCurrentDirectory()).toString()
                    + "is not write-able\nCheck the directory permissions!";
                    JOptionPane.showConfirmDialog(null, msg, "Folder not write-able", JOptionPane.ERROR_MESSAGE);
                  }
              }
          }

      }
  }

  /**
   * Set the contents of the HTMLViewer to the specified text.
   *
   * @param text valid HTML string
   */
  public void setHTML(String text) {
    _textArea.setText(text);
    _textArea.setCaretPosition(0);
  }

  /**
   * Writes the contents of the specified JTextComponent to the specified file.
   *
   * @param file
   * @param jTextComponent
   */
  private void writeFile(File file, JTextComponent jTextComponent) {
    FileWriter writer = null;
    try {
        writer = new FileWriter(file);
        jTextComponent.write(writer);
    }
    catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
    }
    finally {
        if (writer != null) {
            try {
                writer.close();
            }
            catch (IOException e) {
                Message.printWarning(Message.LOG_OUTPUT, "Error while saving Command Status Report", e);
            }
        }
    }
  }
}