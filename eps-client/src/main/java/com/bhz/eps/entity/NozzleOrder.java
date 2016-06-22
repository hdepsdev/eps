package com.bhz.eps.entity;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

public class NozzleOrder implements java.io.Serializable{
	private static final long serialVersionUID = 3883165105572886859L;
	@Getter @Setter
	private String workOrder;
	@Getter @Setter
	private String nozzleNumber;
	@Getter @Setter
	private int orderStatus;
	@Getter @Setter
	private int oilType;
	@Getter @Setter
	private int oilCategory;
	@Getter @Setter
	private int price;
	@Getter @Setter
	private BigDecimal volumeConsume;
	@Getter @Setter
	private String stationCode;
	@Getter @Setter
	private int uploadStatus;
	@Getter @Setter
	private String uploadDateTime;
	
	public static final int ORDER_NOT_PAYED = 0;
	public static final int ORDER_LOCKED = 1;
	public static final int ORDER_PAYED = 2;
	
	public static final int UPLOADED = 0;
	public static final int UN_UPLOAD = 1;
}
