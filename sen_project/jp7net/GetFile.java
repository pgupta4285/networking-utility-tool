import java.awt.*; import java.awt.event.*; import java.io.*;
import javax.swing.*; import java.net.*;

public class GetFile extends JFrame
{
  JTextArea box = new JTextArea("Getting data ...");
  public GetFile ()
  {
    super("Get File Application"); setSize(600,300);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JScrollPane pane = new JScrollPane(box);
    add(pane); setVisible(true);
  }
  void getData(String address) throws MalformedURLException
  {
    setTitle(address); URL page=new URL(address);
    StringBuffer text=new StringBuffer();
    try
    {
      HttpURLConnection conn=(HttpURLConnection) page.openConnection();
      conn.connect();
      InputStreamReader in=new InputStreamReader((InputStream) conn.getContent());
      BufferedReader buff = new BufferedReader(in);
      box.setText("Getting data...");
      String line,key,header; int i=0; //displays headers too
      do {
        key=conn.getHeaderFieldKey(i);
        header=conn.getHeaderField(i);
        if(key==null){key="";}else{key=key+": ";}
        if(header!=null)text.append(key+header+"\n");
        i++;} while (header!=null);
      text.append("\n");
      do {line=buff.readLine();text.append(line+"\n");} while (line!=null);
      box.setText(text.toString());
    }
    catch (IOException ioe) {box.setText(" IO Error: " + ioe.getMessage());}
  }
  public static void main(String args[])
  {
    if (args.length<1)
      {System.out.println("Usage: java GetFile url");System.exit(0);}
    try {GetFile app=new GetFile(); app.getData(args[0]);}
    catch (MalformedURLException mue)
      {System.out.println("Bad URL: "+args[0]);}
  }

}
