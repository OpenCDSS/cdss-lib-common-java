package RTi.Util.IO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.io.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import RTi.Util.GUI.JGUIUtil;

/**
 * A visible component that displays a file in a JFrame.
 * <p>
 * Scrollbars are automatically diplayed as needed
 */
public class HTMLViewer extends JFrame implements ActionListener
{
  public static final int WIDTH  = 500;
  public static final int HEIGHT = 400;
   
  private JEditorPane   _textArea;
  private JButton     _buttonDismiss;
  private JButton _buttonPrint;


  class HTMLViewerAdapter extends WindowAdapter
  {
    public void windowClosing(WindowEvent event)
    {
      setVisible(false);         // hide the Frame
      dispose();
    }
  }

  public HTMLViewer()
  {
    //setTitle(filename);
    
    initGUI();

    pack();
    center();
  }
  
  /**
   * Centers window on screen
   */
  private void center()
  {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension window = this.getSize();
    setLocation((screen.width - window.width) / 2, 
            (screen.height - window.height) / 2);
  }

  
  /**
   * Displays a file in a dialog.
   *
   * @param filename The name of the file to display
   */
  HTMLViewer(String filename)
  {
    setTitle(filename);
    initGUI();

    pack();
    setVisible(true);
  }

  private void initGUI()
  {
	JGUIUtil.setIcon ( this, JGUIUtil.getIconImage() );
    setBackground(Color.lightGray);

    JPanel outerPNL = new JPanel();
    outerPNL.setLayout(new BorderLayout());
    getContentPane().add(outerPNL);

    TitledBorder border = new TitledBorder("");
    outerPNL.setBorder(border);
     
    _textArea = new JEditorPane();
    _textArea.setContentType("text/html");
    _textArea.setEditable(false);
    
    _textArea.setBorder(new EmptyBorder(2,2,2,2));

    // install textArea
    JScrollPane scrollPane = new JScrollPane(_textArea);
    scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    outerPNL.add(scrollPane, BorderLayout.CENTER);

    // install buttonDismiss
    JPanel southPNL = new JPanel();
    southPNL.setLayout(new BorderLayout());
    outerPNL.add(southPNL, BorderLayout.SOUTH);
    
    JPanel controlPNL = new JPanel();
    //controlPNL.setLayout( new BoxLayout( controlPNL, BoxLayout.X_AXIS ) );
    southPNL.add(controlPNL, BorderLayout.CENTER);

    _buttonPrint = new JButton("Print");
    _buttonPrint.addActionListener(this);
    controlPNL.add(_buttonPrint);
    
    _buttonDismiss = new JButton("Close");
    _buttonDismiss.addActionListener(this);
    controlPNL.add(_buttonDismiss);

    // Populate text area from the file
  //   final JTextComponent textpane = _textArea;

//     readFile(filename,textpane );

    this.addWindowListener(new HTMLViewerAdapter());
  }

 
  /**
   * Read file into pane
   *
   * @param filename The name of the file to be displayed
   * @param pane     The JTextComponent to receive the text
   */
  private void  readFile(String filename, JTextComponent textpane)
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
   
  public void actionPerformed(ActionEvent event)
  {
    Object source = event.getSource();
    
    if( source == _buttonDismiss )
      {
        setVisible(false);
        dispose();
      }
    else if (source == _buttonPrint)
      {
        DocumentRenderer renderer = new DocumentRenderer();
        renderer.print( _textArea );
      }
  }



  public static void main(String args[]) 
  {
    // new HTMLViewer("TextSamplerDemoHelp.html");
    HTMLViewer hTMLViewer = new HTMLViewer();
    hTMLViewer.setHTML("<html><body>Hello World</body></html>");
    hTMLViewer.setVisible(true);
  }


  public void setHTML(String text)
  {
    _textArea.setText(text);
    _textArea.setCaretPosition(0);
  }
}

