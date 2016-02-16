package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
		String new_div = "";
		if(img_dir.isDirectory()){
			File[] img_files = img_dir.listFiles();//遍历图片文件
			for(File img_file: img_files){
				if(img_file.getName().startsWith(full_pic_name)){//按文件名匹配图片
					BufferedImage cur_img = ImageIO.read(img_file);//打开图片文件
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
	
	public String insert_sound(String sound_text, String sound_id){
		//读取音频文件，生成一个音频播放按钮
		String full_sound_name = this.sound_keyword + sound_id;
		System.out.println("==========insert sound " + full_sound_name + "==========");
		File sound_dir = new File("src/static/sounds");
		String new_paly_logo = "";
		if(sound_dir.isDirectory()){
			File[] sound_files = sound_dir.listFiles();//遍历音频文件
			for(File sound_file: sound_files){
				//System.out.println(file.getName());
				if(sound_file.getName().startsWith(full_sound_name)){//按文件名匹配音频
					String onclick = "play_sound('../static/sounds/" + sound_file.getName() + "')";
					new_paly_logo = "<img class='play_logo' onclick=" + onclick + " src='../static/img/play.png'/>";
				}
			}
		}
		return new_paly_logo;
	}
	
	public String insert_video(String video_text, String video_id) throws IOException{
		//读取视频文件，生成一个视频页面，并返回打开链接的link
		String full_video_name = this.video_keyword + video_id;
		System.out.println("==========insert video " + full_video_name + "==========");
		File video_dir = new File("src/static/videos");
		String new_video_page = "";
		String new_video_link = "";
		if(video_dir.isDirectory()){
			File[] video_files = video_dir.listFiles();//遍历音频文件
			for(File video_file: video_files){
				if(video_file.getName().startsWith(full_video_name)){//按文件名匹配音频
					new_video_page = "<video class='video_in_text' controls='controls' height='500' style='clear: both;display: block;margin: auto;' src='../static/videos/" + video_file.getName() + "' width='600'></video>";
					RandomAccessFile output_file = new RandomAccessFile("src/output/" + video_file.getName() + ".html", "rw");
					output_file.seek(0);
					output_file.write(new_video_page.getBytes());
					output_file.close();//输出保存html文件
					new_video_link = "<a target='_blank' href='" + video_file.getName() + ".html'>" + video_text + "</a>";
				}
			}
		}
		return new_video_link;
	}
	
	public void handle_html(File text_file) throws IOException{
		this.lcount = 0;
		this.rcount = 0;//清零判定图片插入位置的计数器
		String encoding = "UTF-8"; 
		File html_input = new File("src/rawhtml/base.html");
		Document base_html = Jsoup.parse(html_input, "UTF-8", "http://example.com/");//读取html模板文件
		InputStreamReader base_reader = new InputStreamReader(   
				new FileInputStream(text_file), encoding);   
		BufferedReader basebufferedReader = new BufferedReader(base_reader);   
		
		Element text_box = base_html.select("div#text").first();
	    Element body = base_html.select("body").first();
	    Element title = base_html.select("h3").first(); 
		
		String eachline = basebufferedReader.readLine();  
		title.text(eachline);//设置标题
		
		ArrayList<String> lines = new ArrayList<String>();  //实例化一个数组装文章段落
		while (eachline != null) {   
			if(eachline.length() > 0){
				lines.add(eachline);
			}
			eachline = basebufferedReader.readLine(); 
		} //读段落,存入数组	
		lines.remove(lines.get(0));//删除标题段落（默认为第一段）

		//设置正则匹配符
	    Pattern img_pat = Pattern.compile("\\((\\W+?)\\)\\[" + img_keyword + "(\\S+?)\\]");
		Pattern sound_pat = Pattern.compile("\\((\\W+?)\\)\\[" + sound_keyword + "(\\S+?)\\]");
		Pattern video_pat = Pattern.compile("\\((\\W+?)\\)\\[" + video_keyword + "(\\S+?)\\]");
		//遍历段落进行匹配
		for(String line: lines){
			Matcher img_mat = img_pat.matcher(line);
			Matcher sound_mat = sound_pat.matcher(line);
			Matcher video_mat = video_pat.matcher(line);
			String para = "<p>" + line.trim();
			//匹配图片
			if(img_mat.find()){
				String img_text = img_mat.group(1);
				String img_id = img_mat.group(2);
//				System.out.println(this.lcount + " " + this.rcount);
				//插入图片
				text_box.append(this.insert_img(img_text, img_id, this.lcount, this.rcount));
				//段落文字去掉匹配标示
				para = para.replace(img_mat.group(0), img_mat.group(1));
			}
			//匹配音频
			if(sound_mat.find()){
				String sound_text = sound_mat.group(1);
				String sound_id = sound_mat.group(2);
				//在段落结尾加上图片播放logo
				para += this.insert_sound(sound_text, sound_id);
				para = para.replace(sound_mat.group(0), sound_mat.group(1));
			}
			//匹配视频
			if(video_mat.find()){
				String video_text = video_mat.group(1);
				String video_id = video_mat.group(2);
				//替换段落文字成视频链接
				para = para.replace(video_mat.group(0), this.insert_video(video_text, video_id));
			}
			//加上结尾标识符，添加进文本块
			text_box.append(para + "</p><br>");
		}
		
		//加入音频播放器代码
		File audio_file = new File("src/rawhtml/audio.html");
		InputStreamReader audio_reader = new InputStreamReader(   
				new FileInputStream(audio_file), encoding);   
		BufferedReader audiobufferedReader = new BufferedReader(audio_reader);  
		String audio_str = "";
		eachline = audiobufferedReader.readLine();
		while(eachline!=null){
			audio_str += eachline + "\n";
			eachline = audiobufferedReader.readLine();
		}
		//读取所有段落拼接，加入网页body部分最后
		body.append(audio_str);
		audio_reader.close();
		
		//生成html文件
		FileWriter fileWritter = new FileWriter("src/output/" + text_file.getName().substring(0, text_file.getName().length()-4) + ".html");
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.write(base_html.toString());
		bufferWritter.close();
		base_reader.close();  
	}
	public static void main(String[] args) throws IOException {
		ToHtml th = new ToHtml("pic", "sound", "video");//实例化类，设定判定图片，音频，视频的关键词参数
		File text_dir = new File("src/static/text/");//读取文本文件夹
		if(text_dir.isDirectory()){
			File[] text_files = text_dir.listFiles();//遍历文本文件，依次执行处理
			for(File text_file: text_files){
				if(!text_file.getName().endsWith("~")){
					th.handle_html(text_file);
				}
			}
		}
	}
}
