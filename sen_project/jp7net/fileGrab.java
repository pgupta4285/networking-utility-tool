/* fileGrab.java fetches files from remote site -- 2007 10 29
   It then saves in the current directory or subdirectory.
   Files are downloaded from a whitelist maintained by user. */
import java.io.*; import java.util.*; import java.net.*;
public class fileGrab // the driver function
{
  private static final String USAGE = "Usage: java fileGrab url whitelist";
  public static void main(String args[])
  {
    fileGrab_model model = new fileGrab_model();
    if (args.length<1) {System.out.println(USAGE); System.exit(0);}
    model.openList(args[1]); model.getData(args[0]);
  }
}
class fileGrab_model
{
  private ArrayList<String> pages=new ArrayList<String>();
  //open whitelist and read filenames into an arrayList
  void openList(String fileName)
  {
    String line="";
	try // not founds display msg, processing continues
	{
      FileReader fr = new FileReader (fileName);
      BufferedReader br = new BufferedReader(fr);
      while ((line=br.readLine())!=null) {pages.add(line);}
      br.close();fr.close();   // close the stream and file
    }
    catch (IOException evt) {System.out.println("**"+fileName);}
  }
  // ensure subfolders created to receive info
  void ensureSub(String sub) {boolean s=new File(sub).mkdirs();}
  //connect to remote site and download file
  void getData(String uri)
  {
    int c; String whitePage, line; Iterator it1=pages.iterator();
    if (uri.lastIndexOf("/") < uri.length()) {uri+="/";}
    while(it1.hasNext())
    {
      whitePage = (String) it1.next(); int spot=whitePage.lastIndexOf("/");
      if (spot>-1) {ensureSub(whitePage.substring(0,spot));}
      try
      {
	  URL page = new URL(uri+whitePage);
	  URLConnection conn=(HttpURLConnection) page.openConnection();
      InputStream fr =  conn.getInputStream();
      FileOutputStream fw = new FileOutputStream(whitePage);
      int len = conn.getContentLength();
      while (((c=fr.read()) != -1) && (--len>0))
        {fw.write((char) c);}
      fr.close(); fw.close();
      }
      catch(MalformedURLException mue){System.out.println("Bad URL: "+whitePage);}
      catch(IOException ioe)
           {System.out.println("IO Error: "+ioe.getMessage());
            System.out.println("IO ioe: "+ioe);
			System.out.println("At URL: "+whitePage); }
    }
  }
}
