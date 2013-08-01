package com.kunze.androidlocaltodo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TaskListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_list, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (item.getItemId() == R.id.action_load_astrid)
    	{
    		LoadAstrid();
    	}
		return true;
    }
    
    private void LoadAstrid()
    {
    	// Look for a zip file in the proper location
    	String loc = Environment.getExternalStorageDirectory() + "/AstridImport/";
    	AlertDialog.Builder errorBuilder = new AlertDialog.Builder(this);
    	errorBuilder.setTitle("Error reading Astrid Import!");
    	try
    	{
    		// Try to safely open the file
    		String errText = "Cannot find Astrid file in " + loc;
    		File astridDir = new File(loc);
    		if (!astridDir.isDirectory())
    		{
    			throw new Exception(errText);
    		}
    		
    		File[] files = astridDir.listFiles();
    		if (files.length != 1)
    		{
    			throw new Exception(errText);
    		}
    		
    		File astridFile = files[0];
    		if (!astridFile.isFile())
    		{
    			throw new Exception(errText);
    		}
    		
    		// Try to unzip the file and unpack the tasks
    		errText = "Could not unzip file " + astridFile.getAbsolutePath();
    		InputStream stream = new FileInputStream(astridFile);
    		ZipInputStream zipStream = new ZipInputStream(stream);
    		
    		ZipEntry tasksEntry = null;
    		while ((tasksEntry = zipStream.getNextEntry()) != null)
    		{
    			if (tasksEntry.getName().equals("tasks.csv"))
    			{
    				break;
    			}
    		}
    		if (tasksEntry == null)
    		{
    			throw new Exception(errText);
    		}
    		
    		int size = (int)tasksEntry.getSize();
    		byte tasksContent[] = new byte[size];
    		int offset = 0;
    		while (size != 0)
    		{
    			int read = zipStream.read(tasksContent, offset, size);
    			if (read == 0)
    			{
    				throw new Exception(errText);
    			}
    			offset += read;
    			size -= read;
    		}
    		
    		String tasksString = new String(tasksContent, "UTF-8");
    		
    		// Parse the tasks in the task list
    		errText = "Could not parse task list";
    		List<String> taskLines = new LinkedList<String>(Arrays.asList(tasksString.split("\n")));
    		// Remove the header row
    		taskLines.remove(0);
    		// Some tasks have newlines in quotes, so we have to adjust for that.
    		ListIterator<String> it = taskLines.listIterator();
    		while (it.hasNext())
    		{
    			String task = it.next();
    			while (CountQuotes(task) % 2 == 1)
    			{
    				task += it.next();
    				it.remove();
    			}
    		} 
     		List<String> tasks = new LinkedList<String>();
    		for (String taskLine : taskLines) {
				String taskFields[] = taskLine.split(",");
				tasks.add(taskFields[0]);
			}
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasks.toArray(new String[0]));
    		ListView listView = (ListView)findViewById(R.id.task_list);
    		listView.setAdapter(adapter);
   		
    		zipStream.close();
    	}
    	catch (Exception e)
    	{
    		AlertDialog dlg = errorBuilder.setMessage(e.getMessage()).create();
    		dlg.show();
    	}
    }
    
    static private int CountQuotes(String string)
    {
    	int count = 0;
    	for (int i = 0; i < string.length(); ++i) 
    	{
			switch (string.charAt(i))
			{
			case '\\':
				++i;
				break;
			case '\"':
				++count;
				break;
			default:
				// Nothing
			}
		}
    	return count;
    }
    
}
