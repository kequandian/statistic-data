package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.crud.ExtendedStatistics;
import com.jfeat.am.module.statistics.util.SimpleEncryptionUtil;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api("加密接口测试")
@RestController
@RequestMapping("/api/adm/stat/encryption")
public class StatisticEncryptionEndpoint {

    @Resource
    ExtendedStatistics extendedStatistics;
// 测试时使用
    @GetMapping("/encryption")
    public Tip encryption(@RequestParam (name = "code")String code){
        String encryption = SimpleEncryptionUtil.encryption(code,3);
        return SuccessTip.create(encryption);
    }

    @GetMapping("/decrypt")
    public Tip decrypt(@RequestParam (name = "code")String code){
        String encryption = SimpleEncryptionUtil.decrypt(code,3);
        return SuccessTip.create(encryption);
    }
}
