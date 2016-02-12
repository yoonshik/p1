package tests;

import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A helper class which prints the results of a bidding to an output stream.
 * 
 * By default, it used the standard output.
 *
 */
public class Logger
{
    private static Logger instance = new Logger();
    
    public static Logger getInstance() { return instance; }
    
    private Logger() { }
    
    private PrintStream outStream = System.out;
    
    public void setOutput(PrintStream outStream)
    {
        this.outStream = outStream;
    }
    
    public void logStart(String method, Object... args)
    {
        synchronized (this.outStream)
        {
            String argStr = "";
            if (args.length > 0)
            {
                for (int i=0; i<args.length-1; ++i)
                {
                    argStr += args[i].toString() + ", ";
                }
                argStr += args[args.length-1].toString();
            }
            Date now = new Date();
            
            Format formatter = new SimpleDateFormat("HH:mm:ss.SSS");
            this.outStream.format("S [%s] %s(%s)\n", formatter.format(now), method, argStr);
            this.outStream.flush();
        }
    }
    
    public void logEnd(String method, Object returnValue, Object... args)
    {
        synchronized (this.outStream)
        {
            String argStr = "";
            if (args.length > 0)
            {
                for (int i=0; i<args.length-1; ++i)
                {
                    argStr += args[i].toString() + ", ";
                }
                argStr += args[args.length-1].toString();
            }
            Date now = new Date();
            
            Format formatter = new SimpleDateFormat("HH:mm:ss.SSS");
            if (returnValue == null) { returnValue = ""; }
            this.outStream.format("E [%s] %s(%s) -> %s\n", formatter.format(now), method, argStr, returnValue.toString());
            this.outStream.flush();
        }
    }
}
