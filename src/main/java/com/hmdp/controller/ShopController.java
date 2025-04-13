package com.hmdp.controller;


import com.hmdp.dto.Result;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/shop")
public class ShopController {

    public Result getshopType(){
        return Result.ok();
    }


}
