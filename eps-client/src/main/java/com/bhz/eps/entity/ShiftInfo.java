package com.bhz.eps.entity;

import lombok.Getter;
import lombok.Setter;

public class ShiftInfo implements java.io.Serializable{
	private static final long serialVersionUID = 2642347310271556798L;
	@Getter @Setter
	private byte shiftId;
	@Getter @Setter
	private String shiftName;
	
	public static int SIZE = 16;
}
