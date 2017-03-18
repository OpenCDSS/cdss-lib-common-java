package RTi.Util.IO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
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
 * <p>
 * Scrollbars are automatically displayed as needed
 */
@SuppressWarnings("serial")
public class HTMLViewer extends JFrame implements ActionListener
{
  class HTMLViewerAdapter extends WindowAdapter
  {
    public void windowClosing(WindowEvent event)
    {
      setVisible(false); // hide the Frame
      dispose();
    }
  }

  public static final int HEIGHT = 400;

  public static final int WIDTH = 500;

  public static void main(String args[])
  {
    // new HTMLViewer("TextSamplerDemoHelp.html");
    HTMLViewer hTMLViewer = new HTMLViewer();
    hTMLViewer.setHTML("<html><body>Hello World</body></html>");
    hTMLViewer.setVisible(true);
  }

  private JButton _buttonDismiss;

  private JButton _buttonPrint;

  private JButton _buttonSave;

  private JEditorPane _textArea;

  public HTMLViewer() {
    // setTitle(filename);

    initGUI();

    pack();
    center();
  }

  /**
   * Displays a file in a dialog.
   * 
   * @param filename The name of the file to display
   */
  HTMLViewer(String filename) {
    setTitle(filename);
    initGUI();

    pack();
    setVisible(true);
  }

  public void actionPerformed(ActionEvent event)
  {
    Object source = event.getSource();

    if (source == _buttonDismiss)
      {
        setVisible(false);
        dispose();
      }
    else if (source == _buttonPrint)
      {
        DocumentRenderer renderer = new DocumentRenderer();
        renderer.print(_textArea);
      }
    else if (source == _buttonSave)
      {
        saveToFile();
      }
  }

  /**
   * Centers window on screen
   */
  private void center()
  {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension window = this.getSize();
    setLocation((screen.width - window.width) / 2, (screen.height - window.height) / 2);
  }

  /**
   * Initialize GUI
   */
  private void initGUI()
  {
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

    // install textArea
    JScrollPane scrollPane = new JScrollPane(_textArea);
    scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    outerPNL.add(scrollPane, BorderLayout.CENTER);

    // install buttonDismiss
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

    _buttonDismiss = new JButton("Close");
    _buttonDismiss.addActionListener(this);
    controlPNL.add(_buttonDismiss);

    this.addWindowListener(new HTMLViewerAdapter());
  }

  /**
   * Determines if specified file is write-able.
   * <p>
   * A file is write-able if it:
   * <ul>
   * <li> exists
   * <li> is a file (not a diretory)
   * <li> user has write permission
   * <ul>
   * 
   * @param selectedFile
   * @return true, if file is write-able, otherwise false
   */
  public boolean isWriteable(File selectedFile)
  {

    if (selectedFile != null && (selectedFile.canWrite() == false || selectedFile.isFile() == false))
      {
        // Post an error message and return
        JOptionPane.showMessageDialog(this, "You do not have permission to write to file:" + "\n" + selectedFile.getPath(), "File not Writable",
            JOptionPane.ERROR_MESSAGE);

        return false;
      }
    else
      return true;
  }

  /**
   * Read file into pane.
   * 
   * @param filename name of the file to be displayed
   * @param pane JTextComponent to receive the text
   */
  /* TODO SAM Evaluate whether needed
  private void readFile(String filename, JTextComponent textpane)
  {
    try
      {
        FileReader fr = new FileReader(filename);
        textpane.read(fr, null);
        fr.close();
      }
    catch (IOException e)
      {
        System.err.println(e);
      }
  }
  */

  /**
   * Saves the contents of the HTMLViewer to a file.
   * <p>
   * The user will be prompted where to save the file with a JFileChooser. The
   * JFileChooser will open at the last directory used.
   */
  public void saveToFile()
  {
    JGUIUtil.setWaitCursor(this, true);
    
    JFileChooser jfc = JFileChooserFactory.createJFileChooser(JGUIUtil.getLastFileDialogDirectory());
    jfc.setDialogTitle("Save Command Status Report to File");
    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
    jfc.setSelectedFile(new File(JGUIUtil.getLastFileDialogDirectory()+File.separator + "CommandStatusReport.html"));
    SimpleFileFilter htmlFilter = new SimpleFileFilter("html", "HyperText Markup Language");
    jfc.addChoosableFileFilter(htmlFilter);
    jfc.setAcceptAllFileFilterUsed(false);
 
    JGUIUtil.setWaitCursor(this, false);

    /*
     * The right way to check that file is write-able is to override
     * JFileChooser.approveSelection(). However I am reluctant to modify
     * JFileChooserFactory to use an instance of JFileChooser with
     * approveSelection() overridden to do the checking because of my
     * unfamiliarity with how it is being used
     */
    while (true)
      {
        int retVal = jfc.showSaveDialog(this);

        if (retVal != JFileChooser.APPROVE_OPTION)
          {
            return;
          }
        else
          {
            File file = jfc.getSelectedFile();

            if (file.exists())
              {
                String msg = "File " + file.toString() + " already exists!" 
                + "\n Do you want to overwrite it ?";
                int result = JOptionPane.showConfirmDialog(null, msg, 
                    "File exists", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION)
                  {
                    // Attempt to clear selected file, known not to work in 1.42
                    jfc.setSelectedFile(null);
                    continue;
                  }
                else
                  {
                    if (isWriteable(file))
                      {
                        String currDir = (jfc.getCurrentDirectory()).toString();
                        JGUIUtil.setLastFileDialogDirectory(currDir);

                        writeFile(file, _textArea);
                        return;
                      }
                  }
              }
            else
              {
                if (jfc.getCurrentDirectory().canWrite())
                  {
                    String currDir = (jfc.getCurrentDirectory()).toString();
                    JGUIUtil.setLastFileDialogDirectory(currDir);

                    writeFile(file, _textArea);
                    return;
                  }
                else
                  {
                    String msg = (jfc.getCurrentDirectory()).toString()
                    + "is not write-able"
                    + "\nCheck the directory permissions!";
                    JOptionPane.showConfirmDialog(null, msg, 
                        "Directory not write-able", JOptionPane.ERROR_MESSAGE);
                  }
              }
          }

      } // end of while

  } // eof saveToFile

  /**
   * Set the contents of the HTMLViewer to the specified text.
   *  
   * @param text valid HTML string
   */
  public void setHTML(String text)
  {
    _textArea.setText(text);
    _textArea.setCaretPosition(0);
  }

  /**
   * Writes the contents of the specified JTextComponent to the specified file.
   * 
   * @param file
   * @param jTextComponent
   */
  private void writeFile(File file, JTextComponent jTextComponent)
  {
    FileWriter writer = null;
    try
      {
        writer = new FileWriter(file);
        jTextComponent.write(writer);
      }
    catch (IOException ex)
      {
        JOptionPane.showMessageDialog(this, "File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
      }
    finally
      {
        if (writer != null)
          {
            try
              {
                writer.close();
              }
            catch (IOException e)
              {
              Message.printWarning(Message.LOG_OUTPUT,
                  "Error while saving Command Status Report", e);
              }
          }
      }
  }
}
