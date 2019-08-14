package com.knowology.km.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.knowology.km.dal.Database;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ReadExcel {
	@SuppressWarnings("unchecked")
	public static boolean writeExcel(String dirPath, String fileName,
			String sheetTitle, String title, List columnTitle, List text) {
		WritableWorkbook workBook = null;
		WritableSheet sheet = null;
		if (dirPath == null || "".equals(dirPath) || fileName == null
				|| "".equals(fileName)) {
			System.out.println("建立excel文件失败：路径或文件名为空");
			return false;
		}
		File filePath = new File(dirPath);
		{
			if (!filePath.exists()) {
				// 如果文件要保存的目录不存在则产生该目录
				if (!filePath.mkdir()) {
					System.out.println("建立excel文件失败：无法建立该目录");
					return false;
				}
			}
		}
		// 在该目录下产生要保存的文件名
		String excelPath = dirPath + "/" + fileName + ".xls";
		File excelFile = new File(excelPath);
		// 以下开始输出到EXCEL
		try {
			if (!excelFile.exists()) {
				if (!excelFile.createNewFile()) {
					System.out.println("建立excel文件失败：建立excel文件发生异常");
					return false;
				}
			}
			/** **********创建工作簿************ */
			workBook = Workbook.createWorkbook(excelFile);
			/** **********创建工作表************ */
			if ("".equals(sheetTitle) || sheetTitle == null) {
				sheet = workBook.createSheet("Sheet1", 0);
			} else {
				sheet = workBook.createSheet(sheetTitle, 0);
			}
			// SheetSettings sheetSet = sheet.getSettings();
			// sheetSet.setProtected(false);
			/** ************设置单元格字体************** */
			WritableFont headFont = new WritableFont(WritableFont.ARIAL, 14);
			WritableFont normalFont = new WritableFont(WritableFont.ARIAL, 12);
			/** ************以下设置几种格式的单元格************ */
			// 用于表头
			WritableCellFormat wcf_head = new WritableCellFormat(headFont);
			wcf_head.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
			wcf_head.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
			wcf_head.setAlignment(Alignment.CENTRE); // 文字水平对齐
			wcf_head.setWrap(false); // 文字是否换行

			// 用于正文居中
			WritableCellFormat wcf_center = new WritableCellFormat(normalFont);
			wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条
			wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐
			wcf_center.setAlignment(Alignment.CENTRE); // 文字水平对齐
			wcf_center.setWrap(false); // 文字是否换行

			/** ************单元格格式设置完成****************** */

			/** ***************以下是报表的内容********************* */
			// 合并单元格设置excel的题目
			int x = 0;
			if ("".equals(title) || title == null) {
				title = "";
				x = 0;
			} else {
				if (columnTitle != null && columnTitle.size() > 0) {
					sheet.mergeCells(0, 0, columnTitle.size() - 1, 0);
					sheet.addCell(new Label(0, 0, title, wcf_head));
					x = 1;
				} else if (text != null && text.size() > 0
						&& ((List) text.get(0)).size() > 0) {
					sheet.addCell(new Label(0, ((List) text.get(0)).size() - 1,
							title, wcf_head));
					x = 1;
				} else {
					sheet.addCell(new Label(0, 0, title, wcf_head));
					x = 1;
				}
			}
			// 设置列名
			if (columnTitle != null && columnTitle.size() > 0) {
				for (int i = 0; i < columnTitle.size(); i++) {
					sheet.addCell(new Label(i, x, String.valueOf(columnTitle
							.get(i)), wcf_center));
				}
				x = x + 1;
			}

			// 写入正文数据
			if (text != null && text.size() > 0) {
				for (int i = 0; i < text.size(); i++) {
					List content = (List) text.get(i);
					if (content != null && content.size() > 0) {
						for (int j = 0; j < content.size(); j++) {
							String data = String.valueOf(content.get(j));
							// 判断数据类型
							if (content.get(j) instanceof String) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							} else if (content.get(j) instanceof Integer) {
								Number numberLabel = new Number(j, x + i,
										Integer.parseInt(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Float) {
								Number numberLabel = new Number(j, x + i, Float
										.parseFloat(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Double) {
								Number numberLabel = new Number(j, x + i,
										Double.parseDouble(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Long) {
								Number numberLabel = new Number(j, x + i, Long
										.parseLong(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Short) {
								Number numberLabel = new Number(j, x + i, Short
										.parseShort(data), wcf_center);
								sheet.addCell(numberLabel);
							} else if (content.get(j) instanceof Boolean) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							} else if (content.get(j) instanceof Byte) {
								sheet.addCell(new Label(j, x + i, data,
										wcf_center));
							} else {
								try {
									Number numberLabel = new Number(j, x + i,
											Double.parseDouble(data),
											wcf_center);
									sheet.addCell(numberLabel);
								} catch (Exception ex) {
									sheet.addCell(new Label(j, x + i, data,
											wcf_center));
								}
							}
							sheet.setColumnView(j, data.length() + 10);
						}
					}
				}
			}
			/** **********以上所写的内容都是写在缓存中的，下一句将缓存的内容写到文件中******** */
			workBook.write();
		} catch (Exception e) {
			System.out.println("建立excel文件失败：" + e.getMessage());
			return false;
		} finally {
			/** *********关闭文件************* */
			try {
				if (workBook != null) {
					workBook.close();
				}
			} catch (Exception ex) {
				System.out.println("关闭文件流失败：" + ex.getMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * @param excelFileName
	 *            :excel文件的具体路径+文件名 return List(类型为两级嵌套List)
	 */
	@SuppressWarnings("unchecked")
	public static List<List<Object>> readExcel(File excelFileName) {
		InputStream stream = null;
		Workbook workBook = null;
		List data = new ArrayList();
		if (excelFileName == null || "".equals(excelFileName)) {
			System.out.println("读取excel文件失败：路径或文件名为空");
			return null;
		}
		// File file = new File(excelFileName);
		// if (!file.exists()) {
		// System.out.println("读取excel文件失败：路径或文件名不存在");
		// return null;
		// }
		try {
			stream = new FileInputStream(excelFileName);
			workBook = Workbook.getWorkbook(stream);
			Sheet sheet = workBook.getSheet(0);
			int columns = sheet.getColumns();
			int rows = sheet.getRows();
			for (int i = 0; i < rows; i++) {
				List<Object> row = new ArrayList();
				for (int j = 0; j < columns; j++) {
					Cell cell = sheet.getCell(j, i);
					String cellValue = cell.getContents();
					if (cell.getType() == CellType.NUMBER) {
						try {
							Integer intCell = Integer.valueOf(cellValue);
							row.add(intCell);
						} catch (Exception ex) {
							Double doubleCell = Double.valueOf(cellValue);
							row.add(doubleCell);
						}
					} else {
						row.add(cellValue);
					}
				}
				data.add(row);
			}
		} catch (Exception e) {
			System.out.println("读取excel文件失败：" + e.getMessage());
			return null;
		} finally {
			/** *********关闭流和工作簿************* */
			try {
				if (workBook != null) {
					workBook.close();
				}
				if (stream != null) {
					stream.close();
				}
			} catch (Exception ex) {
				System.out.println("关闭文件流失败：" + ex.getMessage());
				return null;
			}
		}
		return data;
	}

	public static List<List<Object>> readTxt(File excelFileName) {
		List<List<Object>> list = new ArrayList<List<Object>>();
		List<Object> linelist = null;
		String encoding = Database.getJDBCValues("regressqueryreadencoding");
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					excelFileName), encoding);// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			String str[];

			while ((lineTxt = bufferedReader.readLine()) != null) {
				linelist = new ArrayList<Object>();
				try {
					if ("".equals(lineTxt)) {
						continue;
					}
					if (lineTxt.indexOf("\t") != -1) {
						str = lineTxt.split("\t");
						if (str.length == 1) {
							linelist.add(str[0]);
							linelist.add("");

						} else {
							linelist.add(str[0]);
							linelist.add(str[1]);
						}
						list.add(linelist);
					}
					// System.out.println(lineTxt);
				} catch (Exception e) {
					continue;
				}

			}

			read.close();
		} catch (Exception ex) {

			return null;
		}

		return list;

	}

}
