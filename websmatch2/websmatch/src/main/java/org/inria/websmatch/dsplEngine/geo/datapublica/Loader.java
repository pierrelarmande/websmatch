package org.inria.websmatch.dsplEngine.geo.datapublica;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;

public class Loader {

	private String path;

	public Loader(String path) {
		this.setPath(path);
	}

	public List<String[]> load() throws FileNotFoundException {
		
		ArrayList<String[]> res = new ArrayList<String []>();

		// FileInputStream fis = new FileInputStream(datas);
		try {
			//path = "/home/manu/workspace/WebSmatch/war/WEB-INF/classes/"+path;
		    	//path = "/"+path;
			L.Debug(this, "Loading " + path, true);
			InputStream is = this.getClass().getResourceAsStream(path);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);//new FileReader(path));
			String tmp = new String();
			
			br.readLine();
			while ((tmp = br.readLine()) != null) {
				String[] splitted = tmp.split("\\,", -1);
				res.add(splitted);
			}
			br.close();

		} catch (Exception e) {
		    L.Error(e.getMessage(),e);
		}
		return res;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}