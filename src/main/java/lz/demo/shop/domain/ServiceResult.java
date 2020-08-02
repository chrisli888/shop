package lz.demo.shop.domain;


import lombok.Data;

@Data
public class ServiceResult {
    private int code;
    private String desc;
    private Object data;

    public ServiceResult(int code ,String desc)
    {
        this.code = code;
        this.desc = desc;
    }

}
