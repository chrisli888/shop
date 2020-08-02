package lz.demo.shop.controller;

import lz.demo.shop.domain.ServiceResult;
import lz.demo.shop.web.WebApiResponse;
import lz.demo.shop.web.annotation.RequestMapping;
import org.springframework.stereotype.Component;

@RequestMapping(value = "/test")
@Component
public class TestController {

    @RequestMapping(value = "/index",method = "GET")
    public WebApiResponse index()
    {
        return new WebApiResponse(new ServiceResult(10000,"success"));
    }
}
