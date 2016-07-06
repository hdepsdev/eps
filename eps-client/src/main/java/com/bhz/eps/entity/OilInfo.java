package com.bhz.eps.entity;

import lombok.Getter;
import lombok.Setter;

public class OilInfo implements java.io.Serializable{
	private static final long serialVersionUID = -2354146995346188803L;
	@Getter @Setter
	private byte oilId;
	@Getter @Setter
	private String oilName;
	
	public static int SIZE = 16;
}
