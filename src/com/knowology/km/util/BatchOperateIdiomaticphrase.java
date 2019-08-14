package com.knowology.km.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import java.io.File;

import com.jfinal.config.Constants;
import com.jfinal.config.Plugins;
import com.knowology.km.dal.Database;

public class BatchOperateIdiomaticphrase {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		startDatabase();//
		// String filename = "E:\\idiomaticphrase.txt";
		// String filename = "E:\\testtxt.txt";
		String filename = "E:\\关联词.txt";
		// String sql =
		// "insert into m_idiomaticphrase(id,idiomaticphrase,explain,type,source) values(seq_m_idiomaticphrase_id.nextval,?,?,'谚语','民间口语')";
		// String sql =
		// "insert into  m_preposition(PREPOSITIONID,PREPOSITION) values(seq_m_preposition_id.nextval,?)";
		// String
		// sql="insert into m_Adjective(adjectiveid,name,Structure) values(SEQ_m_Adjective_id.nextval,?,'单字式')";
		// String
		// sql="insert into m_interjection(interjectionid,name) values(SEQ_m_interjection_id.nextval,?)";
		String sql = "insert into m_relationword(id,name)  values(seq_m_relationword_id.nextval,?)";
		File file = new File(filename);
		List<List<String>> ret = readTxt(file);

		insert(sql, ret);
	}

	/**
	 * 启动数据连接
	 * */
	private static void startDatabase() {
		Plugins ps = new Plugins();
		SyncConfig c = new SyncConfig();
		c.configConstant(new Constants());
		c.configPlugin(ps);
		for (int i = 0; i < ps.getPluginList().size(); i++) {
			ps.getPluginList().get(i).start();
		}
	}

	/**
	 * 读取数据文件
	 * */
	private static List<List<String>> readTxt(File excelFileName) {
		List<List<String>> ret = new ArrayList<List<String>>();
		// System.out.println("--------------read " + excelFileName+" begin");
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					excelFileName), "GBK");// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			String str[];
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if ("".equals(lineTxt)) {
					continue;
				}

				lineTxt = lineTxt.replaceAll("[\\t|\\n|\\r]", "");
				List<String> lst = null;
				str = lineTxt.split(" ");

				for (String s : str) {
					if ("".equals(s)) {
						continue;
					}
					lst = process(s);
					if (lst == null)
						continue;
					ret.add(lst);
					System.out.println(s);
				}

				// lst=process(lineTxt);
				// if(lst==null)continue;

			}
			read.close();
		} catch (Exception ex) {
			return null;
		}
		// System.out.println("--------------read " + excelFileName+" end");
		return ret;
	}

	/**
	 * 批量插入数据库中
	 * */
	private static int insert(String sql, List<List<String>> lstlst) {
		List<Object> lstpara = null;
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		int r = 0;
		for (int i = 0; i < lstlst.size(); i++) {
			lstpara = new ArrayList<Object>();
			List<String> lst = lstlst.get(i);
			for (int j = 0; j < lst.size(); j++) {
				lstpara.add(lst.get(j));
				System.out.println(lst.get(j));
			}
			lstLstpara.add(lstpara);
		}
		r += Database.executeNonQueryBatchTransaction(sql, lstLstpara);
		if (r > 0) {
			System.out.println("--------------OK--------------");

		}
		return r;
	}

	private static List<String> process(String lineTxt) {
		List<String> ret = new ArrayList<String>();
		try {
			if ("".equals(lineTxt)) {
				return null;
			}
			if (lineTxt.indexOf("\r") != -1 || lineTxt.indexOf("\n") != -1) {
				return null;
			}
			// String str[];
			// if (lineTxt.indexOf("——") != -1) {
			// str = lineTxt.replaceAll(" ", "").split("——");
			// ret.add(str[0]);
			// ret.add(str[1]);
			// return ret;
			// }
			// else{
			// return null;
			// }
			ret.add(lineTxt.replaceAll(" ", ""));

			return ret;
		} catch (Exception e) {
			return null;
		}
	}

}
