package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.SAXException;

public class ToPdf {
	String img_keyword, sound_keyword, video_keyword;
	int lcount, rcount;

	public ToPdf(String img_keyword, String sound_keyword, String video_keyword){
		this.img_keyword = img_keyword;
		this.sound_keyword = sound_keyword;
		this.video_keyword = video_keyword;
		this.lcount = 0;
		this.rcount = 0;
	}
	
	public void change_img_size(BufferedImage raw_img, String new_pic_url) throws IOException{
		//处理需要缩小的图片
		System.out.println("==========change img size==========");
		//获取原始图片长宽
	    int row_width = raw_img.getWidth(), row_height = raw_img.getHeight();
	    //计算缩小的比例
	    float radio = (float) row_width/300;
	    //计算缩小后的长宽
	    int new_width = 300, new_height = (int) (row_height/radio);
	    
	    Image small_img = raw_img.getScaledInstance(new_width, new_height, Image.SCALE_SMOOTH);//缩小
	    BufferedImage new_img = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB);
        Graphics g = new_img.getGraphics();
        g.drawImage(small_img, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        ImageIO.write(new_img, "JPEG", new File(new_pic_url));// 输出到文件流
	}
	
	public String insert_img(String img_text, String img_id, int lcount, int rcount) throws IOException{
		//插入图片函数
		String full_pic_name = this.img_keyword + img_id;//获取不带后缀的文件全名
		System.out.println("==========insert img " + full_pic_name + "==========");
		File img_dir = new File("src/static/pics");
		String new_block = "<fo:block>\n<fo:external-graphic ";
		if(img_dir.isDirectory()){
			File[] img_files = img_dir.listFiles();//遍历图片文件
			for(File img_file: img_files){
				if(img_file.getName().startsWith(full_pic_name)){//按文件名匹配图片
					BufferedImage cur_img = ImageIO.read(img_file);//打开图片文件
					if(cur_img.getWidth() > 400 && cur_img.getWidth() > cur_img.getHeight()){
						new_block += "text-align=\"center\" " +
									"width=\"50%\" " +
									"content-height=\"50%\" " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/" +
									img_file.getName() + "')\"\n/>" + "</fo:block>";
					}else if(cur_img.getWidth() > 400 && cur_img.getWidth() < cur_img.getHeight()){
						//改图片大小
						this.change_img_size(cur_img, "src/static/pics/small_" + img_file.getName());
						if(lcount > rcount){
							new_block += "text-align=\"right\" " +
									"width=\"50%\" " +
									"content-height=\"50%\" " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/small_" +
									img_file.getName() + "')\"/>" + "</fo:block>";
							this.rcount += 1;
						}else{
							new_block += "text-align=\"left\" " +
									"width=\"50%\" " +
									"content-height=\"50%\" " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/small_" +
									img_file.getName() + "')\"/>" + "</fo:block>";
							this.lcount += 1;
						}
					}else{
						if(lcount > rcount){
							new_block += "text-align=\"right\" " +
									"width=\"50%\" " +
									"content-height=\"50%\" " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/" +
									img_file.getName() + "')\"/>" + "</fo:block>";
							this.rcount += 1;
						}else{
							new_block += "text-align=\"left\" " +
									"width=\"50%\" " +
									"content-height=\"50%\" " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/" +
									img_file.getName() + "')\"/>" + "</fo:block>";
							this.lcount += 1;
						}
					}
				}
			}
		}
		return new_block;
	}
	
	public void handle_pdf(File text_file) throws IOException{
		this.lcount = 0;
		this.rcount = 0;//清零判定图片插入位置的计数器
		String encoding = "UTF-8"; 
		InputStreamReader base_reader = new InputStreamReader(   
				new FileInputStream(text_file), encoding);   
		BufferedReader basebufferedReader = new BufferedReader(base_reader);   
		
		String all_fo = " ";
		String root_head = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n";
		String layout_master_str = "<fo:layout-master-set>\n" +
									"<fo:simple-page-master master-name=\"simple\" " +
					                  "page-height=\"29.7cm\" " +
					                  "page-width=\"21cm\" " +
					                  "margin-top=\"1cm\" " +
					                  "margin-bottom=\"2cm\" " +
					                  "margin-left=\"2.5cm\" " +
					                  "margin-right=\"2.5cm\">" +
								      "<fo:region-body margin-top=\"3cm\"/>\n" +
								      "<fo:region-before extent=\"3cm\"/>\n" +
								      "<fo:region-after extent=\"1.5cm\"/>\n" +
								    "</fo:simple-page-master>" +
								  "</fo:layout-master-set>";
		String flow_head = "<fo:page-sequence master-reference=\"simple\">\n" +
							"<fo:flow flow-name=\"xsl-region-body\">\n";
		String title_block = "<fo:block font-size=\"18pt\" " +
					            "font-family=\"kaiti\" " +
					            "line-height=\"24pt\" " +
					            "space-after.optimum=\"15pt\" " +
					            "background-color=\"blue\" " +
					            "color=\"white\" " +
					            "text-align=\"center\" " +
					            "padding-top=\"3pt\">\n";
		
		String eachline = basebufferedReader.readLine();  
		title_block += eachline + "</fo:block>\n";
		ArrayList<String> lines = new ArrayList<String>();  //实例化一个数组装文章段落
		while (eachline != null) {   
			if(eachline.length() > 0){
				lines.add(eachline);
			}
			eachline = basebufferedReader.readLine(); 
		} //读段落,存入数组	
		lines.remove(lines.get(0));//删除标题段落（默认为第一段）
		all_fo = root_head + layout_master_str + flow_head + title_block;
		String line_block_head = "<fo:block font-size=\"12pt\" " +
				                "font-family=\"kaiti\" " +
				                "line-height=\"15pt\" " +
				                "space-after.optimum=\"3pt\" " +
				                "text-align=\"justify\">\n";
		//设置正则匹配符
	    Pattern img_pat = Pattern.compile("\\((\\W+?)\\)\\[" + img_keyword + "(\\S+?)\\]");
	    Pattern sound_pat = Pattern.compile("\\((\\W+?)\\)\\[" + sound_keyword + "(\\S+?)\\]");
		Pattern video_pat = Pattern.compile("\\((\\W+?)\\)\\[" + video_keyword + "(\\S+?)\\]");
	    //遍历段落进行匹配
		for(String line: lines){
			Matcher img_mat = img_pat.matcher(line);
			Matcher sound_mat = sound_pat.matcher(line);
			Matcher video_mat = video_pat.matcher(line);
			String line_block = line_block_head + line.trim();
			//匹配图片
			if(img_mat.find()){
				String img_text = img_mat.group(1);
				String img_id = img_mat.group(2);
				//插入图片
				//段落文字去掉匹配标示
				line_block += this.insert_img(img_text, img_id, lcount, rcount);
				line_block = line_block.replace(img_mat.group(0), img_mat.group(1));
			}
			if(sound_mat.find()){
				//替换标示文字
				line_block = line_block.replace(sound_mat.group(0), sound_mat.group(1));
			}
			//匹配视频
			if(video_mat.find()){
				//替换标示文字
				line_block = line_block.replace(video_mat.group(0), video_mat.group(1));
			}
			//加上结尾标识符，添加进文本块
			 all_fo += line_block + "</fo:block>\n";
		}
		all_fo += "</fo:flow>\n</fo:page-sequence>\n</fo:root>";
		System.out.println(all_fo);
		//生成html文件
		RandomAccessFile output_file = new RandomAccessFile("src/output/" + text_file.getName() + ".fo", "rw");
		output_file.seek(0);
		output_file.write(all_fo.getBytes());
		output_file.close();//输出保存html文件
		base_reader.close();  
	}
	
	public static void main(String[] args) throws IOException, SAXException, TransformerException {
		ToPdf tp = new ToPdf("pic", "sound", "video");//实例化类，设定判定图片，音频，视频的关键词参数
		File text_dir = new File("src/static/text/");//读取文本文件夹
		if(text_dir.isDirectory()){
			File[] text_files = text_dir.listFiles();//遍历文本文件，依次执行处理
			for(File text_file: text_files){
				if(!text_file.getName().endsWith("~")){
					//处理文本文件转换成fo文件
					tp.handle_pdf(text_file);
				}
			}
		}
		//设置转换配置文件
		FopFactory fopFactory = FopFactory.newInstance(new File("/home/zhufree/Tools/fop-2.1/conf/fop.xconf"));
		//设置输出文件
		
	    //设置源文件
		File fo_dir = new File("src/output/");
	    if(text_dir.isDirectory()){
			File[] fo_files = fo_dir.listFiles();//遍历文本文件，依次执行处理
			for(File fo_file: fo_files){
				if(fo_file.getName().endsWith(".fo")){
					OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("src/output/" + fo_file.getName() + ".pdf")));
					Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
				    TransformerFactory factory = TransformerFactory.newInstance();
				    Transformer transformer = factory.newTransformer(); // identity transformer
					//处理文本文件转换成fo文件
					Source src = new StreamSource(fo_file);
				    Result res = new SAXResult(fop.getDefaultHandler());
				    //转换
				    transformer.transform(src, res);
				    out.close();
				}
			}
		}
	    
    }
}
