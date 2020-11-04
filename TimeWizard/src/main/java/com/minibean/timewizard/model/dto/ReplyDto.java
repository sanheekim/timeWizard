package com.minibean.timewizard.model.dto;

public class ReplyDto {

	private int reply_no;
	private String notice_no;
	private String user_no;
	private String reply_content;
	private String reply_regdate;
	
	
	public ReplyDto() {
		
	}
	
	public ReplyDto(int reply_no, String notice_no, String user_no, String reply_content, String reply_regdate) {
		super();
		this.reply_no = reply_no;
		this.notice_no = notice_no;
		this.user_no = user_no;
		this.reply_content = reply_content;
		this.reply_regdate = reply_regdate;
	}

	public int getReply_no() {
		return reply_no;
	}

	public void setReply_no(int reply_no) {
		this.reply_no = reply_no;
	}

	public String getNotice_no() {
		return notice_no;
	}

	public void setNotice_no(String notice_no) {
		this.notice_no = notice_no;
	}

	public String getUser_no() {
		return user_no;
	}

	public void setUser_no(String user_no) {
		this.user_no = user_no;
	}

	public String getReply_content() {
		return reply_content;
	}

	public void setReply_content(String reply_content) {
		this.reply_content = reply_content;
	}

	public String getReply_regdate() {
		return reply_regdate;
	}

	public void setReply_regdate(String reply_regdate) {
		this.reply_regdate = reply_regdate;
	}
	
	
	
	
}
