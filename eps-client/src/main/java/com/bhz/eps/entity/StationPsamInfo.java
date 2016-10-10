package com.bhz.eps.entity;

import lombok.Getter;
import lombok.Setter;

public class StationPsamInfo implements java.io.Serializable {
	private static final long serialVersionUID = 5137133649440061025L;
	@Getter @Setter
	private String stationCode;
	@Getter @Setter
	private String stationName;
	@Getter @Setter
	private int stationStatus;
	@Getter @Setter
	private String stationAddress;
	@Getter @Setter
	private String stationTelephone;
	@Getter @Setter
	private String stationManager;
	@Getter @Setter
	private String ownerCode;
	@Getter @Setter
	private String establishedDate;
}
