package org.fastlsh.query;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import gnu.trove.map.hash.TLongObjectHashMap;

import org.fastlsh.util.Neighbor;

public class Output {
	public void writeSerializedOutput(String file, TLongObjectHashMap<Neighbor []> similars) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(similars);
		oos.flush();
		oos.close();
	}
	
	public void writeText(String file, TLongObjectHashMap<Neighbor []> similars) throws IOException {
		;
	}
	
	public void writeJson(String file, TLongObjectHashMap<Neighbor []> similars) throws IOException {
		;
	}
}
