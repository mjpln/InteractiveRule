package com.knowology.km.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.log4j.Logger;

import com.knowology.km.dal.Database;

public class KMDBOperation {
	public static Logger logger = Logger.getLogger(Database.class);
	

	/**
	 * 
	 *描述：判断一个词条是否存在
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:21:38
	 *@param wordclass
	 *@return boolean
	 */
	public static boolean IsWordClassExist(String wordclass) {
		try {
			String sql = "select * from wordclass where wordclass='"
					+ wordclass + "'";
			Result rst = Database.executeQuery(sql);
			if (rst == null || rst.getRowCount() == 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error(e.toString());
			return false;
		}
	}

	/**
	 * 
	 *描述：
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:20:47
	 *@param id
	 */
	public static void DeleteWordByID(String id) {
		try {
			String sql = "delete from word where wordid='" + id + "'";
			if (Database.executeNonQuery(sql) == -1) {
				logger.error(">>sql:" + sql);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	/**
	 * 
	 *描述：将词类表的数据读入内存
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:23:06
	 *@return Result
	 */
	public static Result GetWordTable() {
		try {
			Result rst = null;
			String sql = "select * from word";
			rst = Database.executeQuery(sql);
			return rst;
		} catch (Exception e) {
			logger.error(e.toString());
			return null;
		}
	}

	/**
	 * 
	 *描述：通过词类名获取词类ID
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:23:42
	 *@param wordClass
	 *@return String
	 */
	public static String GetWordClassIDByWordClass(String wordClass) {
		try {
			String sql = "select wordclassid from wordclass where wordclass='"
					+ wordClass + "'";
			Result rst = Database.executeQuery(sql);
			if (rst == null || rst.getRowCount() == 0) {
				return "";
			}
			SortedMap[] sMap = rst.getRows();
			return sMap[0].get("wordclassid").toString().trim();
		} catch (Exception e) {
			logger.error(e.toString());
			return "";
		}
	}

	/**
	 * 
	 *描述：根据词类名和词条名获取词条ID
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:24:25
	 *@param wordClass
	 *@param word
	 *@return String
	 */
	public static String GetWordIDByWordAWordClass(String wordClass, String word) {
		try {
			String wordClassID = GetWordClassIDByWordClass(wordClass);
			if (wordClassID.equals("")) {
				return "";
			}
			String sql = "select wordid from word where wordclassid="
					+ wordClassID + " and word='" + word + "'";
			Result rst = Database.executeQuery(sql);
			if (rst == null || rst.getRowCount() == 0) {
				return "";
			}
			SortedMap[] sMap = rst.getRows();
			return sMap[0].get("wordclassid").toString().trim();
		} catch (Exception e) {
			logger.error(e.toString());
			return "";
		}
	}

	/**
	 * 
	 *描述：给word表去重
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:29:03
	 */
	public static void RemoveDuplicate4Word() {
		Result rst = GetWordTable();

		HashMap<String, ArrayList<String>> record = new HashMap<String, ArrayList<String>>();
		for (SortedMap row : rst.getRows()) {
			String key = row.get("word").toString().trim() + "_"
					+ row.get("wordclassid").toString().trim() + "_"
					+ row.get("stdwordid").toString().trim();
			if (record.containsKey(key)) {
				record.get(key).add(row.get("wordid").toString().trim());
			} else {
				java.util.ArrayList<String> lst = new java.util.ArrayList<String>();
				lst.add(row.get("wordid").toString().trim());
				record.put(key, lst);
			}

		}
		for (Entry<String, java.util.ArrayList<String>> iter : record
				.entrySet()) {
			if (iter.getValue().size() >= 2) {
				for (int i = 1; i < iter.getValue().size(); i++) {
					DeleteWordByID(iter.getValue().get(i));
				}
			}
		}
	}

	/**
	 * 
	 *描述：获取表序列的下一个值
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:33:42
	 *@return String
	 */
	public static String GetSeqWordIDNextVal() {
		try {
			String sql = "select seq_word_id.nextval from dual";
			Result rst = Database.executeQuery(sql);
			if (rst == null || rst.getRowCount() == 0) {
				return "";
			}
			return rst.getRowsByIndex()[0][0].toString().trim();
		} catch (Exception e) {
			logger.error(e.toString());
			return "";
		}
	}

	/**
	 * 
	 *描述：获取最大词条ID
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:36:03
	 *@return String
	 */
	public static String GetMaxWordID() {
		try {
			String sql = "select max(wordid) from word";
			Result rst = Database.executeQuery(sql);
			if (rst == null || rst.getRowCount() == 0) {
				return "";
			}
			return rst.getRowsByIndex()[0][0].toString().trim();
		} catch (Exception e) {
			logger.error(e.toString());
			return "";
		}
	}

	/**
	 * 
	 *描述：获取最大词类ID
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:40:58
	 *@return
	 */
	public static String GetMaxWordClassID() {
		try {
			String sql = "select max(wordClassid) from wordclass";
			Result rst = Database.executeQuery(sql);
			if (rst == null || rst.getRowCount() == 0) {
				return "";
			}
			return rst.getRowsByIndex()[0][0].toString().trim();
		} catch (Exception e) {
			logger.error(e.toString());
			return "";
		}
	}

	/**
	 * 
	 *描述：新建序列SEQ_WORD_ID
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:41:47
	 */
	public static void CreateOrReplaceSeqWordID() {
		String sql = "drop sequence seq_word_id";
		String maxWordID = GetMaxWordID();
		if (maxWordID.equals("")) {
			logger.error(">>Fail to CreateOrReplaceSeqWordID!");
			return;
		}
		String sql2 = "create sequence SEQ_WORD_ID minvalue 0 maxvalue 99999999 start with "
				+ maxWordID + " increment by 1 cache 20 cycle";
		List<String> lst = new ArrayList<String>();
		lst.add(sql);
		lst.add(sql2);
		if (Database.ExecuteSQL(lst) == false) {
			logger.error(">>" + sql2);
		}
	}

	/**
	 * 
	 *描述：新建序列SEQ_WORDClass_ID
	 * 
	 * @author: qianlei
	 *@date： 日期：2014-6-16 时间：下午03:41:47
	 */
	public static void CreateOrReplaceSeqWordClassID() {
		String sql = "drop sequence seq_wordclass_id";
		String maxWordID = GetMaxWordClassID();
		if (maxWordID.equals("")) {
			logger.error(">>Fail to CreateOrReplaceSeqWordID!");
			return;
		}
		String sql2 = "create sequence SEQ_WORDClass_ID minvalue 0 maxvalue 99999999 start with "
				+ maxWordID + " increment by 1 cache 20 cycle";
		java.util.ArrayList<String> lst = new java.util.ArrayList<String>();
		lst.add(sql);
		lst.add(sql2);
		if (Database.ExecuteSQL(lst) == false) {
			logger.error(">>" + sql2);
		}
	}

	/**
	 * 如果wordID存在库中，将别名插入，返回1； 否则，返回0。 另外注意，不对otherName去重
	 * 
	 * @param wordID
	 * @param otherName
	 * @param type
	 * @return
	 */
	public static int AddOtherName4WordID(String wordID,
			ArrayList<String> otherName, String otherNameType) {
		try {
			String sql1 = "select * from word where wordid = " + wordID;
			Result rst = Database.executeQuery(sql1);
			ArrayList<String> sqlList = new ArrayList<String>();
			if (rst == null || rst.getRowCount() == 0) // 不存在该词，进行插入
			{
				return 0;
			}
			SortedMap[] sMap = rst.getRows();
			String wordClassID = sMap[0].get("wordclassid").toString().trim();
			// 查出已有的别名
			String sql = "Select x.wordid, x.wordclassid wordclassid, wordclass, x.word, x.type, y.word stdword";
			sql += " from Word x, word y, WordClass";
			sql += " where WordClass.WordClassID = x.WordClassID";
			sql += " and x.stdwordid = y.wordid";
			sql += " and x.type != '标准名称'";
			sql += " and x.stdwordid=" + wordID;
			Result synWordTable = Database.executeQuery(sql);
			HashMap<String, String> synDic = new HashMap<String, String>();
			for (SortedMap row : synWordTable.getRows()) {
				String wrd = row.get("word").toString();
				if (!synDic.containsKey(wrd)) {
					synDic.put(wrd, "");
				}
			}

			String sql6 = "update word set operationType='A' where wordid="
					+ wordID;
			sqlList.add(sql6);
			for (String str : otherName) {
				if (synDic.containsKey(str)) {
					String sql5 = "update word set operationType='A' where word = '"
							+ str + "' and stdwordid=" + wordID;
					sqlList.add(sql5);
				} else {
					String sql4 = "select seq_word_id.nextval from dual";
					Result dt1 = Database.executeQuery(sql4);
					String wordid2 = String.valueOf(dt1.getRowsByIndex()[0][0]);
					String sql5 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
					sql5 = sql5 + wordid2 + "," + wordClassID + ", '" + str
							+ "','" + otherNameType + "','" + wordID + "', "
							+ "0,'" + "A" + "')";
					sqlList.add(sql5);
				}
			}
			// 执行sql集
			if (Database.ExecuteSQL(sqlList) == false) {
				logger.error(">>插入别名出错：" + wordID + "_" + otherName + "_"
						+ wordClassID);
				return 0;
			}
			return 1;
		} catch (Exception e) {
			logger.error(e.toString());
			return 0;
		}
	}

	// 重载
	public static int AddOtherName4WordID(String wordID,
			ArrayList<String> otherName) {
		try {
			String otherNameType = "其他别名";
			String sql1 = "select * from word where wordid = " + wordID;
			Result rst = Database.executeQuery(sql1);
			ArrayList<String> sqlList = new ArrayList<String>();
			if (rst == null || rst.getRowCount() == 0) // 不存在该词，进行插入
			{
				return 0;
			}
			SortedMap[] sMap = rst.getRows();
			String wordClassID = sMap[0].get("wordclassid").toString().trim();
			// 查出已有的别名
			String sql = "Select x.wordid, x.wordclassid wordclassid, wordclass, x.word, x.type, y.word stdword";
			sql += " from Word x, word y, WordClass";
			sql += " where WordClass.WordClassID = x.WordClassID";
			sql += " and x.stdwordid = y.wordid";
			sql += " and x.type != '标准名称'";
			sql += " and x.stdwordid=" + wordID;
			Result synWordTable = Database.executeQuery(sql);
			HashMap<String, String> synDic = new HashMap<String, String>();
			for (SortedMap row : synWordTable.getRows()) {
				String wrd = row.get("word").toString();
				if (!synDic.containsKey(wrd)) {
					synDic.put(wrd, "");
				}
			}

			String sql6 = "update word set operationType='A' where wordid="
					+ wordID;
			sqlList.add(sql6);
			for (String str : otherName) {
				if (synDic.containsKey(str)) {
					String sql5 = "update word set operationType='A' where word = '"
							+ str + "' and stdwordid=" + wordID;
					sqlList.add(sql5);
				} else {
					String sql4 = "select seq_word_id.nextval from dual";
					Result dt1 = Database.executeQuery(sql4);
					String wordid2 = String.valueOf(dt1.getRowsByIndex()[0][0]);
					String sql5 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
					sql5 = sql5 + wordid2 + "," + wordClassID + ", '" + str
							+ "','" + otherNameType + "','" + wordID + "', "
							+ "0,'" + "A" + "')";
					sqlList.add(sql5);
				}
			}
			// 执行sql集
			if (Database.ExecuteSQL(sqlList) == false) {
				logger.error(">>插入别名出错：" + wordID + "_" + otherName + "_"
						+ wordClassID);
				return 0;
			}
			return 1;
		} catch (Exception e) {
			logger.error(e.toString());
			return 0;
		}
	}

	/**
	 * 更新WORD中的信息，如果不存在word，则进行插入；否则将otherNames进行插入，
	 * 如果word存在别名，则将otherNames中的并且库中不存在的别名插入；
	 * 
	 * @param word
	 * @param otherName
	 *            如果存在多个别名，则以&&分隔
	 * @param wordClass
	 * @param operationType
	 * @return 插入失败为0，否则为成功
	 */
	public static int UpdateWord(String word, String otherNames,
			String wordClass, String operationType, String otherNameType) {
		try {
			int wordCNT = 0;
			word = word.replace("'", "''");
			otherNames = otherNames.replace("'", "''");
			if (word.equals("") || wordClass.equals("")) {
				return 0;
			}
			String sql0 = "select * from wordclass where wordclass='"
					+ wordClass + "'";
			Database db = new Database();
			Result rst = Database.executeQuery(sql0);
			if (rst == null || rst.getRowCount() == 0) {
				logger.error(">>需要批量导入的词类：" + wordClass + "不存在！" + sql0);
				return 0;
			}
			SortedMap[] sMap = rst.getRows();
			String wordClassId = sMap[0].get("wordclassid").toString().trim();
			// 找到词ID
			String sql1 = "select * from word where wordclassid='"
					+ wordClassId + "' and word='" + word
					+ "' and (type='标准名称' OR type is null)";
			rst = Database.executeQuery(sql1);
			ArrayList<String> sqlList = new ArrayList<String>();
			if (rst == null || rst.getRowCount() == 0) // 不存在该词，进行插入
			{

				String sql2 = "select seq_word_id.nextval from dual";
				Result rst1 = Database.executeQuery(sql2);
				String wordid = String.valueOf(rst1.getRowsByIndex()[0][0]);
				String sql3 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
				sql3 = sql3 + wordid + "," + wordClassId + ", '" + word + "','"
						+ "标准名称" + "'," + "''" + ", " + "0,'" + operationType
						+ "')";
				sqlList.add(sql3);
				ArrayList<String> item1 = StringOper.StringSplit(otherNames,
						"&&");
				wordCNT++; // 记录增加或更新的词条数
				for (String str : item1) {
					String sql4 = "select seq_word_id.nextval from dual";
					rst1 = Database.executeQuery(sql4);
					String wordid2 = String
							.valueOf(rst1.getRowsByIndex()[0][0]);
					String sql5 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
					sql5 = sql5 + wordid2 + "," + wordClassId + ", '" + str
							+ "','" + otherNameType + "'," + wordid + ", "
							+ "0,'" + "A" + "')";
					sqlList.add(sql5);
					wordCNT++;
				}
			} else { // 存在该词，进行更新(包括更新别名，更新操作OT)
				sMap = rst.getRows();
				String wordid = sMap[0].get("wordid").toString();
				// 查出已有的别名
				String sql = "Select x.wordid, x.wordclassid wordclassid, wordclass, x.word, x.type, y.word stdword";
				sql += " from Word x, word y, WordClass";
				sql += " where WordClass.WordClassID = x.WordClassID";
				sql += " and x.stdwordid = y.wordid";
				sql += " and x.type != '标准名称'";
				sql += " and x.stdwordid=" + wordid.toString();
				Result synWordTable = Database.executeQuery(sql);
				HashMap<String, String> synDic = new HashMap<String, String>();
				for (SortedMap row : synWordTable.getRows()) {
					String wrd = row.get("word").toString();
					if (!synDic.containsKey(wrd)) {
						synDic.put(wrd, "");
					}
				}
				ArrayList<String> item1 = StringOper.StringSplit(otherNames,
						"&&");

				String sql6 = "update word set operationType='A' where wordid="
						+ wordid;
				sqlList.add(sql6);
				wordCNT++; // 记录增加或更新的词条数
				for (String str : item1) {
					if (synDic.containsKey(str)) {
						String sql5 = "update word set operationType='A' where word = '"
								+ str + "' and stdwordid=" + wordid;
						sqlList.add(sql5);
						wordCNT++; // 记录增加或更新的词条数
					} else {
						String sql4 = "select seq_word_id.nextval from dual";
						Result rst1 = Database.executeQuery(sql4);
						String wordid2 = String
								.valueOf(rst1.getRowsByIndex()[0][0]);
						String sql5 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
						sql5 = sql5 + wordid2 + "," + wordClassId + ", '" + str
								+ "','" + otherNameType + "','" + wordid
								+ "', " + "0,'" + "A" + "')";
						sqlList.add(sql5);
						wordCNT++; // 记录增加或更新的词条数
					}
				}
			}
			// 执行sql集
		
			if (Database.ExecuteSQL(sqlList) == false) {
				logger.error(">>数据批量导入出错：" + word + "_" + otherNames + "_"
						+ wordClass + "_" + operationType);
				return 0;
			}
			return wordCNT;
		} catch (Exception e) {
			logger.error(e.toString());
			return 0;
		}
	}

	// 重载
	public static int UpdateWord(String word, String otherNames,
			String wordClass, String operationType) {
		try {
			String otherNameType = "其他别名";
			int wordCNT = 0;
			word = word.replace("'", "''");
			otherNames = otherNames.replace("'", "''");
			if (word.equals("") || wordClass.equals("")) {
				return 0;
			}
			String sql0 = "select * from wordclass where wordclass='"
					+ wordClass + "'";
			Database db = new Database();
			Result rst = Database.executeQuery(sql0);
			if (rst == null || rst.getRowCount() == 0) {
				logger.error(">>需要批量导入的词类：" + wordClass + "不存在！" + sql0);
				return 0;
			}
			SortedMap[] sMap = rst.getRows();
			String wordClassId = sMap[0].get("wordclassid").toString().trim();
			// 找到词ID
			String sql1 = "select * from word where wordclassid='"
					+ wordClassId + "' and word='" + word
					+ "' and (type='标准名称' OR type is null)";
			rst = Database.executeQuery(sql1);
			ArrayList<String> sqlList = new ArrayList<String>();
			if (rst == null || rst.getRowCount() == 0) // 不存在该词，进行插入
			{

				String sql2 = "select seq_word_id.nextval from dual";
				Result rst1 = Database.executeQuery(sql2);
				String wordid = String.valueOf(rst1.getRowsByIndex()[0][0]);
				String sql3 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
				sql3 = sql3 + wordid + "," + wordClassId + ", '" + word + "','"
						+ "标准名称" + "'," + "''" + ", " + "0,'" + operationType
						+ "')";
				sqlList.add(sql3);
				ArrayList<String> item1 = StringOper.StringSplit(otherNames,
						"&&");
				wordCNT++; // 记录增加或更新的词条数
				for (String str : item1) {
					String sql4 = "select seq_word_id.nextval from dual";
					rst1 = Database.executeQuery(sql4);
					String wordid2 = String
							.valueOf(rst1.getRowsByIndex()[0][0]);
					String sql5 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
					sql5 = sql5 + wordid2 + "," + wordClassId + ", '" + str
							+ "','" + otherNameType + "'," + wordid + ", "
							+ "0,'" + "A" + "')";
					sqlList.add(sql5);
					wordCNT++;
				}
			} else { // 存在该词，进行更新(包括更新别名，更新操作OT)
				sMap = rst.getRows();
				String wordid = sMap[0].get("wordid").toString();
				// 查出已有的别名
				String sql = "Select x.wordid, x.wordclassid wordclassid, wordclass, x.word, x.type, y.word stdword";
				sql += " from Word x, word y, WordClass";
				sql += " where WordClass.WordClassID = x.WordClassID";
				sql += " and x.stdwordid = y.wordid";
				sql += " and x.type != '标准名称'";
				sql += " and x.stdwordid=" + wordid.toString();
				Result synWordTable = Database.executeQuery(sql);
				HashMap<String, String> synDic = new HashMap<String, String>();
				for (SortedMap row : synWordTable.getRows()) {
					String wrd = row.get("word").toString();
					if (!synDic.containsKey(wrd)) {
						synDic.put(wrd, "");
					}
				}
				ArrayList<String> item1 = StringOper.StringSplit(otherNames,
						"&&");

				String sql6 = "update word set operationType='A' where wordid="
						+ wordid;
				sqlList.add(sql6);
				wordCNT++; // 记录增加或更新的词条数
				for (String str : item1) {
					if (synDic.containsKey(str)) {
						String sql5 = "update word set operationType='A' where word = '"
								+ str + "' and stdwordid=" + wordid;
						sqlList.add(sql5);
						wordCNT++; // 记录增加或更新的词条数
					} else {
						String sql4 = "select seq_word_id.nextval from dual";
						Result rst1 = Database.executeQuery(sql4);
						String wordid2 = String
								.valueOf(rst1.getRowsByIndex()[0][0]);
						String sql5 = "Insert into word (wordid,wordclassid,word,type,stdwordID,filledflag,operationType) values(";
						sql5 = sql5 + wordid2 + "," + wordClassId + ", '" + str
								+ "','" + otherNameType + "','" + wordid
								+ "', " + "0,'" + "A" + "')";
						sqlList.add(sql5);
						wordCNT++; // 记录增加或更新的词条数
					}
				}
			}
			// 执行sql集
			if (Database.ExecuteSQL(sqlList) == false) {
				logger.error(">>数据批量导入出错：" + word + "_" + otherNames + "_"
						+ wordClass + "_" + operationType);
				return 0;
			}
			return wordCNT;
		} catch (Exception e) {
			logger.error(e.toString());
			return 0;
		}
	}

	/**
	 * 更新word表中的一条记录，如果不存在该记录，则进行插入，否则，更新其及其别名
	 * 
	 * @param word
	 *            需要更新词条
	 * @param otherNameDic
	 *            别名
	 * @param wordClass
	 *            当前词所属词类名
	 * @param operationType
	 *            更新类型
	 * @return 如果更新成功，则返回0，否则返回1
	 */
	public static int UpdateWord(String word,
			java.util.HashMap<String, Double> otherNameDic, String wordClass,
			String operationType, String otherNameType) {
		String otherName = "";
		for (Entry<String, Double> iter : otherNameDic.entrySet()) {
			otherName += iter.getKey();
			otherName += "&&";
		}
		return UpdateWord(word, otherName, wordClass, operationType,
				otherNameType);
	}

	// 重载
	public static int UpdateWord(String word,
			java.util.HashMap<String, Double> otherNameDic, String wordClass,
			String operationType) {
		String otherNameType = "其他别名";
		String otherName = "";
		for (Entry<String, Double> iter : otherNameDic.entrySet()) {
			otherName += iter.getKey();
			otherName += "&&";
		}
		return UpdateWord(word, otherName, wordClass, operationType,
				otherNameType);
	}

	/**
	 * 更新WORD中的信息，如果不存在word，则进行插入；否则将otherNames进行插入，
	 * 如果word存在别名，则将otherNames中的并且库中不存在的别名插入；
	 * 
	 * @param word
	 * @param otherNameDic
	 * @param wordClass
	 * @param operationType
	 * @param otherNameType
	 * @return
	 */
	public static int UpdateWord(String word, ArrayList<String> otherNameDic,
			String wordClass, String operationType, String otherNameType) {
		String otherName = "";
		for (String iter : otherNameDic) {
			otherName += iter;
			otherName += "&&";
		}
		return UpdateWord(word, otherName, wordClass, operationType,
				otherNameType);
	}

	// 重载
	public static int UpdateWord(String word, ArrayList<String> otherNameDic,
			String wordClass, String operationType) {
		String otherNameType = "其他别名";
		String otherName = "";
		for (String iter : otherNameDic) {
			otherName += iter;
			otherName += "&&";
		}
		return UpdateWord(word, otherName, wordClass, operationType,
				otherNameType);
	}
	
}
