package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/shop")
public class ShopController {
    @Autowired
    private IShopService shopService;

    @GetMapping("/of/type")
    public Result getshopType(@RequestParam("typeId")Integer typeid,@RequestParam("current") Integer current){
        Page page = shopService.selectListByTypeid(typeid, current);
        System.out.println(page);
        return Result.ok(page.getRecords());
    }

    @GetMapping("{id}")
    public Result getbyid(@PathVariable("id") Integer id){

        return shopService.getByIdAndCashe(id);

    }

    @PutMapping("/update")
    public Result updatebyid(@RequestBody Shop shop){

        return shopService.updateAndDeteleCashe(shop);

    }


}
