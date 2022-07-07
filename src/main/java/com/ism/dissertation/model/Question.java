package com.ism.dissertation.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Data
@Component
@Entity
@Table(name = "questions")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int quesId;
	private String title;
	private String optionA;
	private String optionB;
	private String optionC;
	private String optionD;
	private int ans;
	private int chose;

	public Question() {
		super();
	}

	public Question(int quesId, String title, String optionA, String optionB, String optionC, String optionD, int ans, int chose) {
		super();
		this.quesId = quesId;
		this.title = title;
		this.optionA = optionA;
		this.optionB = optionB;
		this.optionC = optionC;
		this.optionD = optionD;
		this.ans = ans;
		this.chose = chose;
	}

}




