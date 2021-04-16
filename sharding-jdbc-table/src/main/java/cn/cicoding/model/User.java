package cn.cicoding.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = -1205226416664488559L;

    private Long id;

    private String city = "";

    private String name = "";

    private Date createTime;
}