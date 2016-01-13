package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ToHtml {
	String img_keyword, sound_keyword, video_keyword;
	int lcount, rcount;

	public ToHtml(String img_keyword, String sound_keyword, String video_keyword){
		this.img_keyword = img_keyword;
		this.sound_keyword = sound_keyword;
		this.video_keyword = video_keyword;
		this.lcount = 0;
		this.rcount = 0;
	}
	
	public void change_img_size(BufferedImage raw_img, String new_pic_url) throws IOException{
		System.out.println("==========change img size==========");
	    int row_width = raw_img.getWidth(), row_height = raw_img.getHeight();
	    float radio = (float) row_width/300;
	    int new_width = 300, new_height = (int) (row_height/radio);
	    //System.out.println(new_width + " " + new_height);
	    Image small_img = raw_img.getScaledInstance(new_width, new_height, Image.SCALE_SMOOTH);//缩小
	    BufferedImage new_img = new BufferedImage(new_width, new_height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = new_img.getGraphics();
        g.drawImage(small_img, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        ImageIO.write(new_img, "JPEG", new File(new_pic_url));// 输出到文件流
	}
	
	public String insert_img(String img_text, String img_id, int lcount, int rcount) throws IOException{
		String full_pic_name = "pic" + img_id;
		System.out.println("==========insert img " + full_pic_name + "==========");
		File img_dir = new File("src/static/pics");
		String new_div = "";
		if(img_dir.isDirectory()){
			File[] img_files = img_dir.listFiles();//遍历图片文件
			for(File img_file: img_files){
				//System.out.println(file.getName());
				if(img_file.getName().startsWith(full_pic_name)){//按文件名匹配图片
					BufferedImage cur_img = ImageIO.read(img_file);//打开图片文件
					//System.out.println(cur_img.getHeight()+" "+cur_img.getWidth());
					if(cur_img.getWidth() > 400 && cur_img.getWidth() > cur_img.getHeight()){
						new_div = "<div class='pic_in_text_center'><img src='../static/pics/" 
								+ img_file.getName() + "' alt=''></div>";
					}else if(cur_img.getWidth() > 400 && cur_img.getWidth() < cur_img.getHeight()){
						//改图片大小
						this.change_img_size(cur_img, "src/static/pics/small_" + img_file.getName());
						if(lcount > rcount){
							new_div = "<div class='pic_in_text_right'><img src='../static/pics/small_" 
									+ img_file.getName() + "' alt=''></div>";
							this.rcount += 1;
						}else{
							new_div = "<div class='pic_in_text_left'><img src='../static/pics/small_" 
									+ img_file.getName() + "' alt=''></div>";
							this.lcount += 1;
						}
					}else{
						if(lcount > rcount){
							new_div = "<div class='pic_in_text_right'><img src='../static/pics/" 
									+ img_file.getName() + "' alt=''></div>";
							this.rcount += 1;
						}else{
							new_div = "<div class='pic_in_text_left'><img src='../static/pics/" 
									+ img_file.getName() + "' alt=''></div>";
							this.lcount += 1;
						}
					}
				}
			}
		}
		return new_div;
	}
	
	public void insert_sound(){
		
	}
	
	public void insert_video(){
		
	}
	
	public void handle_html(File text_file) throws IOException{
		this.lcount = 0;
		this.rcount = 0;
		String encoding = "UTF-8"; 
		File html_input = new File("src/rawhtml/base.html");
		Document base_html = Jsoup.parse(html_input, "UTF-8", "http://example.com/");//读取html模板文件
		InputStreamReader read = new InputStreamReader(   
				new FileInputStream(text_file), encoding);   
		BufferedReader bufferedReader = new BufferedReader(read);   
		String eachline = bufferedReader.readLine();  
		ArrayList<String> lines = new ArrayList<String>();  //实例化一个数组装文章段落
		lines.add(eachline);
		while (eachline != null) {   
			System.out.println(eachline.toString().trim()); 
			if(eachline.length() > 0){
				lines.add(eachline);
			}
			eachline = bufferedReader.readLine(); 
		} //读段落。存入数组
		Element title = base_html.select("h3").first(); 
		title.text(lines.get(0));//设置标题
		lines.remove(0);
		Element text_box = base_html.select("div#text").first();
		//System.out.println(text_box.toString());
	    Element js_box = base_html.select("script#main").first();
	    
	    Pattern img_pat = Pattern.compile("\\((\\W+?)\\)\\[" + img_keyword + "(\\S+?)\\]");
		Pattern sound_pat = Pattern.compile("\\((\\W+?)\\)\\[" + sound_keyword + "(\\S+?)\\]");
		Pattern video_pat = Pattern.compile("\\((\\W+?)\\)\\[" + video_keyword + "(\\S+?)\\]");
//		Matcher m = img_pat.matcher(line);
		
		for(String line: lines){
			Matcher img_mat = img_pat.matcher(line);
			Matcher sound_mat = sound_pat.matcher(line);
			Matcher video_mat = video_pat.matcher(line);
			String para = "<p>" + line.trim() + "</p><br/>";
			if(img_mat.find()){
				System.out.println(img_mat.group(1));
				String img_text = img_mat.group(1);
				String img_id = img_mat.group(2);
//				this.insert_img(img_text, img_id, lcount, rcount);
				System.out.println(this.lcount + " " + this.rcount);
				text_box.append(this.insert_img(img_text, img_id, this.lcount, this.rcount));
				para = para.replace(img_mat.group(0), img_mat.group(1));
			}
			if(sound_mat.find()){
				para = para.replace(sound_mat.group(0), sound_mat.group(1));
			}
			if(video_mat.find()){
				para = para.replace(video_mat.group(0), video_mat.group(1));
			}
//			System.out.println(para);
			text_box.append(para);
		}
		System.out.println(base_html);
		RandomAccessFile output_file = new RandomAccessFile("src/output/" + text_file.getName() + ".html", "rw");
		output_file.seek(0);
		output_file.write(base_html.html().getBytes());
		output_file.close();//输出保存html文件
		read.close();  
	}
	public static void main(String[] args) throws IOException {
		ToHtml th = new ToHtml("pic", "sound", "video");//实例化类，设定判定图片，音频视频的关键词参数
		
		//System.out.println(base_html.toString());
		File text_dir = new File("src/static/text/");//读取文本文件夹
		if(text_dir.isDirectory()){
			File[] text_files = text_dir.listFiles();//遍历文本文件，执行处理
			for(File text_file: text_files){
				//System.out.println(file.getName());
				if(!text_file.getName().endsWith("~")){
					th.handle_html(text_file);
				}
			}
		}
	}
}
