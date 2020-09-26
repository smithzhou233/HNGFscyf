package com.hngf.web.controller.danger;

import com.hngf.common.utils.Constant;
import com.hngf.common.utils.PageUtils;
import com.hngf.common.utils.R;
import com.hngf.common.validator.Assert;
import com.hngf.common.validator.ValidatorUtils;
import com.hngf.entity.danger.Hidden;
import com.hngf.entity.danger.HiddenAccept;
import com.hngf.entity.danger.HiddenRetify;
import com.hngf.entity.danger.HiddenReview;
import com.hngf.service.danger.HiddenAcceptService;
import com.hngf.service.danger.HiddenRetifyService;
import com.hngf.service.danger.HiddenReviewService;
import com.hngf.service.danger.HiddenService;
import com.hngf.web.controller.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 隐患管理
 * @author zhangfei
 * @date 2020-06-10
 */
@RestController
@RequestMapping("scyf/hidden")
@Api(value="隐患管理",tags = {"隐患管理"})
public class HiddenController extends BaseController {

    private static int REVIEW_ENTRUST = 1;//委托评审
    private static int RETIFY_ENTRUST = 2;//委托整改
    private static int ACCEPT_ENTRUST = 3;//委托验收

    @Autowired
    private HiddenService hiddenService;
    @Autowired
    private HiddenReviewService hiddenReviewService;
    @Autowired
    private HiddenRetifyService hiddenRetifyService;
    @Autowired
    private HiddenAcceptService hiddenAcceptService;


    /**
     * 我的待办列表
     */
    @GetMapping(value = "/MyTodoList")
    @RequiresPermissions("scyf:hidden:myTodoList")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "查询类型 0.未处理 1.已处理 ", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "groupId", value = "组织ID", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "status", value = "隐患状态0: 待提交；1: 待评审；2：待整改；3：待验收；4：验收通过；", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "hiddenCatId", value = "隐患类型", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "inspectDefId", value = "检查表", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "pageNum", value = "第几页", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条数据", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "keyword", value = "查询关键字", required = false, paramType = "query", dataType = "string")
    })
    @ApiOperation(value="隐患列表",response = Hidden.class, produces="application/json",notes = "" +
            "隐患登记关联接口如下：<br>" +
            "1、查询隐患等级数据字典：/sys/common/getDictByType?dictType=hidden_level <br>"+
            "2、查询隐患状态数据字典   /sys/common/getDictByType?dictType=hidden_status_undo/hidden_status_done/hidden_status_nomal  <br>"+
            "3、查询当前部门下子部门    /sys/group/getSubGroupList（下拉） /sys/common//getGroupLists （树形）<br>"+
             "4.查询检查表(设置pagesize 大一些)     /scyf/inspectdef/list  <br>"+
            "3、查询隐患类型：/sys/common/getCommonClassifyByType?classifyType=1 <br>"

    )
    public R MyTodoList(@RequestParam Map<String, Object> params,HttpServletRequest req) {
        try {
            Assert.isNull(params.get("type"),"待办类型不能为空");
            Map<String, Object> map = params;
            map.put("createdBy", super.getUserId());
            map.put("userId",super.getUserId());
           // map.put("overTimeRetify", 1);
            map.put("module", 1);
            if(null==params.get("groupId") ){
                map.put("groupId",getGroupId());
            }
            int pageNum = null!= params.get(Constant.PAGE_PAGENUM) ? Integer.parseInt(params.get(Constant.PAGE_PAGENUM).toString()) : 1;
            int pageSize = null != params.get(Constant.PAGE_PAGESIZE) ? Integer.parseInt(params.get(Constant.PAGE_PAGESIZE).toString()) : Constant.PAGE_SIZE;
            params.put("companyId", getCompanyId());
            PageUtils page = hiddenService.queryMyTodoPage(map,pageNum,pageSize,null);
            return R.ok().put("data", page).put("pathUrl",req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/source");
        } catch (Exception var6) {
            var6.printStackTrace();
            return R.error("我的待办列表查询数据出错");
        }
    }




    /**
     * 列表
     */
    @GetMapping(value = "/list")
    @RequiresPermissions("scyf:hidden:list")
    @ApiOperation(value="列表",response = Hidden.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "隐患状态0: 待提交；1: 待评审；2：待整改；3：待验收；4：验收通过；5：验收不通过；6：已撤消；7：已删除；", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "groupId", value = "组织ID", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "hiddenCatId", value = "隐患类型", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "inspectDefId", value = "检查表", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "pageNum", value = "第几页", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条数据", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "keyword", value = "查询关键字", required = false, paramType = "query", dataType = "string")
    })
    public R list(@RequestParam Map<String, Object> params,HttpServletRequest req){
        int pageNum = null!= params.get(Constant.PAGE_PAGENUM) ? Integer.parseInt(params.get(Constant.PAGE_PAGENUM).toString()) : 1;
        int pageSize = null != params.get(Constant.PAGE_PAGESIZE) ? Integer.parseInt(params.get(Constant.PAGE_PAGESIZE).toString()) : Constant.PAGE_SIZE;
        Assert.isNull(params.get("status"),"隐患状态不能为空");
        params.put("companyId", getCompanyId());
        PageUtils page = hiddenService.queryPage(params,pageNum,pageSize,null);

        return R.ok().put("data", page).put("currentUserId",getUserId()).put("pathUrl",req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/source");
    }
    /**
    * @Author: zyj
    * @Description:年度统计 隐患列表查询
    * @Param
    * @Date 15:03 2020/6/29
    */
    @GetMapping(value = "/queryHdangerRecordByPage")
    @RequiresPermissions("scyf:hidden:list")
    @ApiOperation(value="年度统计 隐患列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "组织ID", required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "pageNum", value = "第几页", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "pageSize", value = "每页多少条数据", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "hiddenYear", value = "年份,年度统计要传", required = true, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "yearStatisticsRecord", value = "默认为1,年度统计要传", required = true,defaultValue = "1",paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "typeStatisticsRecord", value = "默认为1,类型统计要传", required = true,defaultValue = "1",paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间,类型统计要传", required = false,paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间，类型统计要传", required = false,paramType = "query", dataType = "String"),

    })
    public R queryHdangerRecordByPage(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest req){
        int pageNum = null!= params.get(Constant.PAGE_PAGENUM) ? Integer.parseInt(params.get(Constant.PAGE_PAGENUM).toString()) : 1;
        int pageSize = null != params.get(Constant.PAGE_PAGESIZE) ? Integer.parseInt(params.get(Constant.PAGE_PAGESIZE).toString()) : Constant.PAGE_SIZE;
        params.put("statistics",1);
        params.put("companyId",getCompanyId());
        params.put("webModule",1);
        if (params.get("groupId")==null){
            params.put("groupId",getGroupId());
        }
        PageUtils page = hiddenService.queryPage(params,pageNum,pageSize,null);
        return R.ok().put("data", page).put("path",req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/source");
    }


    /**
     * 信息
     */
    @GetMapping(value="/info/{hiddenId}")
    @RequiresPermissions("scyf:hidden:info")
    @ApiOperation(value="隐患信息",response = Hidden.class,notes = "" +
            "隐患信息详情接口关联接口如下：<br>" +
            "1、查询评审记录：scyf/hidden/getReviewRecord <br>" +
            "2、查询整改记录：scyf/hidden/getRetifyRecord <br>" +
            "3、查询整改验收记录：scyf/hidden/getAcceptRecord <br>"
    )
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "path", dataType = "int")
    public R info(@PathVariable("hiddenId") Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        return R.ok().put("data", hiddenService.getById(hiddenId));
    }


    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("scyf:hidden:save")
    @ApiOperation(value="保存", produces="application/json",notes = "" +
            "隐患登记关联接口如下：<br>" +
            "1、检查隐患是否可以评审：scyf/hidden/checkedHiddenReview <br>" +
            "2、查询隐患等级数据字典：/sys/common/getDictByType?dictType=hidden_level <br>" +
            "3、查询隐患类型：/sys/common/getCommonClassifyByType?classifyType=1 <br>" +
            "4、查询整改部门/验收部门/所属部门列表：/sys/common/getCompanyGroupLists <br>" +
            "5、查询整改人员/验收人员列表：sys/common/getUserByGroupId <br>"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "隐患所属单位", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "riskPointId", value = "危险点ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectDefId", value = "检查表ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectScheduleId", value = "任务ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectRecordId", value = "检查记录Id", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectItemId", value = "检查项ID/风险ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectContentId", value = "检查内容ID/风险管控措施ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectContentDesc", value = "检查项内容/管控措施内容", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenCatId", value = "隐患类型ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenTitle", value = "隐患名称", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenDesc", value = "隐患描述", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "riskLevel", value = "风险等级", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewGroup", value = "评审单位", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewBy", value = "评审人", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyGroup", value = "整改部门", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenQuondamRetifyBy", value = "整改人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyDeadline", value = "整改期限", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "status", value = "隐患状态", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "isRetify", value = "是否整改", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "isScheduleCheck", value = "是否进行任务检查：1检查,0不检查", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectRecordNo", value = "检查记录编号", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectRecordLogId", value = "检查记录日志ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "itemDetailId", value = "", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAttachPath", value = "隐患附件地址，多个逗号分隔", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "inspectDefType", value = "检查类型", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "spotData", value = "现场数据采集", required = false, paramType = "json", dataType = "string")
    })
    public R save(@RequestBody Hidden hidden){
       /* JSONObject jsonObject=new JSONObject(map);
        Hidden hidden=JSON.toJavaObject(jsonObject,Hidden.class);
        String hiddenRetifyDeadline = map.get("hiddenRetifyDeadline").toString();
        hiddenRetifyDeadline = hiddenRetifyDeadline.replace("Z", "").replace("T"," ").substring(0,19);;*///
     //   hidden.setHiddenRetifyDeadline(DateUtils.stringToDate(hiddenRetifyDeadline,DateUtils.DATE_TIME_PATTERN));

        ValidatorUtils.validateEntity(hidden);

        hidden.setCheckResult(Constant.CHECK_RESULT_CZYH);
        if (null == hidden.getInspectScheduleId()) {
            hidden.setInspectScheduleId(0L);
        }
        if (null == hidden.getIsRetify()) {
            hidden.setIsRetify(0);
        }
        if (null == hidden.getInspectDefId()) {
            hidden.setInspectDefId(0L);
        }
        if (null == hidden.getInspectRecordId()) {
            hidden.setInspectRecordId(0L);
        }
        if (null == hidden.getRiskPointId()) {
            hidden.setRiskPointId(0L);
        }
        if (null == hidden.getInspectRecordNo()) {
            hidden.setInspectRecordNo(0L);
        }

        hidden.setDelFlag(0);
        hidden.setCreatedBy(getUserId());
        hidden.setCreatedTime(new Date());
        hidden.setCompanyId(getCompanyId());
        try {
            hiddenService.save(hidden,getUser().getUserType());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败");
        }
        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("scyf:hidden:update")
    @ApiOperation(value="修改", produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "groupId", value = "隐患所属单位", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "riskPointId", value = "危险点ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectDefId", value = "检查表ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectScheduleId", value = "任务ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectRecordId", value = "检查记录Id", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectItemId", value = "检查项ID/风险ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectContentId", value = "检查内容ID/风险管控措施ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectContentDesc", value = "检查项内容/管控措施内容", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenCatId", value = "隐患类型ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenTitle", value = "隐患名称", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenDesc", value = "隐患描述", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "riskLevel", value = "风险等级", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewGroup", value = "评审单位", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewBy", value = "评审人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyGroup", value = "整改部门", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenQuondamRetifyBy", value = "整改人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyDeadline", value = "整改期限", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "status", value = "隐患状态", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAcceptedGroup", value = "验收单位", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAcceptedBy", value = "验收人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "programmeId", value = "隐患整改方案表ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "happenedTime", value = "发生时间", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "finishedTime", value = "结束时间", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "isRetify", value = "是否整改", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "isScheduleCheck", value = "是否进行任务检查：1检查,0不检查", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectRecordNo", value = "检查记录编号", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "inspectRecordLogId", value = "检查记录日志ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "itemDetailId", value = "", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAttachPath", value = "隐患附件地址，多个逗号分隔", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "inspectDefType", value = "检查类型", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "spotData", value = "现场数据采集", required = false, paramType = "json", dataType = "string")
    })
    public R update(@RequestBody Hidden hidden){
        ValidatorUtils.validateEntity(hidden);
        Assert.isNull(hidden.getHiddenId(),"隐患ID不能为空");

        Hidden hd = hiddenService.getHiddenById(hidden.getHiddenId());
        if (null == hd) {
            return R.error("查无数据");
        }
        //如果status不等于0，不能修改
        if (hd.getStatus().intValue() != Constant.DANGER_NOT) {
            return R.error("不是【提交】流程，不能编辑！");
        }
        hidden.setUpdatedBy(getUserId());
        hidden.setUpdatedTime(new Date());
        hidden.setCompanyId(getCompanyId());
        try {
            hiddenService.update(hidden,getUser().getUserType());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("更新失败");
        }
        return R.ok();
    }

    /**
     * 隐患评审
     * @return
     */
    @PostMapping("/saveReview")
    @ApiOperation(value="隐患评审", produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewGroup", value = "评审单位", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewBy", value = "评审人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyGroup", value = "整改部门", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyBy", value = "整改人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewResult", value = "评审结果 0未通过；1通过", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewRemark", value = "隐患评审说明", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenRetifyDeadline", value = "整改期限", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "programmeId", value = "隐患整改方案表ID", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAcceptedGroup", value = "验收单位", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAcceptedBy", value = "验收人", required = false, paramType = "json", dataType = "int")
    })
    public R saveReview(@RequestBody HiddenReview hiddenReview){
        Long hiddenId = hiddenReview.getHiddenId();
        Assert.isNull(hiddenId,"隐患ID不能为空");
        Hidden hd = this.hiddenService.getHiddenById(hiddenId);

        if (null == hd) {
            return R.error("隐患数据不存在");
        }

        if (hd.getStatus().intValue() != Constant.DANGER_DPS) {
            return R.error("当前状态不允许【评审】，请核实！");
        }

        if (null == hiddenReview.getProgrammeId()) {
            hiddenReview.setProgrammeId(0L);
        }

        hiddenReview.setCreatedBy(getUserId());
        hiddenReview.setCreatedTime(new Date());
        hiddenReview.setCompanyId(getCompanyId());
        hiddenReview.setGroupId(getGroupId());
        try {
            this.hiddenReviewService.save(hiddenReview,getUser().getUserType());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败");
        }
        return R.ok();
    }

    /**
     * 隐患整改
     * @param hiddenRetify
     * @return
     */
    @PostMapping("/saveRetify")
    @ApiOperation(value="隐患整改", produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyReasons", value = "原因分析", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenRetifyMeasures", value = "整改措施", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenRetifyAssisting", value = "整改协助单位", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenRetifyType", value = "整改类型，0、自行整改，1、协助整改,2、委托整改", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyAmount", value = "整改花费", required = true, paramType = "json", dataType = "double"),
            //@ApiImplicitParam(name = "hiddenRetifyDeadline", value = "整改期限", required = false, paramType = "json", dataType = "date"),
            @ApiImplicitParam(name = "retifyUploadPath", value = "整改附件地址，多个逗号分隔", required = false, paramType = "json", dataType = "string")

    })
    public R saveRetify(@RequestBody HiddenRetify hiddenRetify) {
        Long hiddenId = hiddenRetify.getHiddenId();
        Assert.isNull(hiddenId,"隐患ID不能为空");
        Assert.isNull(hiddenRetify.getHiddenRetifyType(),"整改类型不能为空");
        Assert.isNull(hiddenRetify.getHiddenRetifyAmount(),"整改花费不能为空");
        try {
            Hidden hd = this.hiddenService.getHiddenById(hiddenId);
            if (null == hd) {
                return R.error("隐患数据不存在");
            }

            if (hd.getStatus().intValue() != Constant.DANGER_DZG && hd.getStatus().intValue() != Constant.DANGER_YSBTG) {
                return R.error("未到【整改】流程，请核实！");
            }


            hiddenRetify.setCreatedBy(getUserId());
            hiddenRetify.setCompanyId(getCompanyId());
            hiddenRetify.setHiddenRetifyGroup(getGroupId());
            hiddenRetify.setDelFlag(0);
            this.hiddenRetifyService.saveRetify(hiddenRetify);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败");
        }

        return R.ok();
    }

    /**
     * 隐患验收
     * @param hiddenAccept
     * @return
     */
    @PostMapping("/saveAccept")
    @ApiOperation(value="隐患验收", produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "retifyUploadPath", value = "整改附件地址，多个逗号分隔", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenAcceptedDesc", value = "验收说明", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenAcceptedResult", value = "验收结果：4通过；5不通过", required = true, paramType = "json", dataType = "int")

    })
    public R saveAccept(@RequestBody HiddenAccept hiddenAccept) {
        Long hiddenId = hiddenAccept.getHiddenId();
        Assert.isNull(hiddenId,"隐患ID不能为空");

        try {
            Hidden hd = this.hiddenService.getHiddenById(hiddenId);

            if (null == hd) {
                return R.error("隐患数据不存在");
            }

            if (hd.getStatus().intValue() != Constant.DANGER_DYS.intValue()) {
                return R.error("未到【验收】流程，请核实！");
            }

            hiddenAccept.setCreatedBy(getUserId());
            hiddenAccept.setGroupId(getGroupId());
            hiddenAccept.setDelFlag(0);
            hiddenAccept.setCreatedTime(new Date());

            this.hiddenAcceptService.saveAccept(hiddenAccept);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败");
        }

        return R.ok();
    }


    /**
     * 隐患委托处理
     * @return
     */
    @PostMapping("/saveEntrust")
    @ApiOperation(value="隐患委托处理", produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "entrustType", value = "委托类型：1委托评审,2委托整改,3委托验收", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "entrustGroup", value = "委托部门", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "entrustUserId", value = "委托人", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "entrustUserName", value = "委托人名称", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "entrustDesc", value = "委托说明", required = false, paramType = "json", dataType = "string")
    })
    public R saveEntrust(@RequestBody Map<String,Object> params){
        //委托类型
        Assert.isNull(params.get("hiddenId"),"隐患ID不能为空");
        Assert.isNull(params.get("entrustType"),"委托类型不能为空");
        Assert.isNull(params.get("entrustUserId"),"委托评审人不能为空");

        Long hiddenId = Long.parseLong(params.get("hiddenId").toString());
        Hidden hd = this.hiddenService.getHiddenById(hiddenId);

        if (null == hd) {
            return R.error("隐患数据不存在");
        }

        Integer entrustType = Integer.parseInt(params.get("entrustType").toString());

        params.put("isEntrust",1);
        params.put("userId", getUserId());
        params.put("companyId", getCompanyId());
        params.put("groupId", getGroupId());

        try {
            if (entrustType == REVIEW_ENTRUST){
                this.hiddenReviewService.entrustHiddenReview(params);
            } else if (entrustType == RETIFY_ENTRUST){
                this.hiddenRetifyService.entrustHiddenRetify(params);
            } else if (entrustType == ACCEPT_ENTRUST){
                this.hiddenAcceptService.entrustHiddenAccept(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("保存失败");
        }
        return R.ok();
    }

    /**
     * 撤销隐患
     */
    @RequestMapping(value="/cancel",method = RequestMethod.PUT)
    @RequiresPermissions("scyf:hidden:list")
    @ApiOperation(value="撤销隐患",httpMethod = "PUT")
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", paramType = "form", required = true, dataType = "int")
    public R cancel(Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        int res = 0;
        try {
            res = hiddenService.cancel(hiddenId,getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("撤销失败");
        }
        return res > 0 ? R.ok() : R.error("撤销失败");
    }

    /**
     * 删除
     */
    @RequestMapping(value="/delete",method = RequestMethod.DELETE)
    @RequiresPermissions("scyf:hidden:delete")
    @ApiOperation(value="删除",httpMethod = "DELETE")
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", paramType = "form", required = true, dataType = "int")
    public R delete(Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        int res = 0;
        try {
            res = hiddenService.removeById(hiddenId,getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("删除失败");
        }
        return res > 0 ? R.ok() : R.error("删除失败");
    }

    /**
     * 永久删除
     */
    @RequestMapping(value="/permanentDelete",method = RequestMethod.DELETE)
    @RequiresPermissions("scyf:hidden:delete")
    @ApiOperation(value="永久删除",httpMethod = "DELETE")
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", paramType = "form", required = true, dataType = "int")
    public R permanentDelete(Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        int res = hiddenService.permanentRemoveById(hiddenId);
        return res > 0 ? R.ok() : R.error("删除失败");
    }

    /**
     * 删除所有隐患数据
     */
    @RequestMapping(value="/deleteAll",method = RequestMethod.DELETE)
    @RequiresPermissions("scyf:hidden:delete")
    @ApiOperation(value="删除所有隐患数据",httpMethod = "DELETE")
    public R deleteAll(){
        int res = hiddenService.deleteAll(getCompanyId(),getUserId());
        return res > 0 ? R.ok() : R.error("删除失败");
    }


    /**
     * 检查隐患是否可以评审
     */
    @GetMapping(value="/checkedHiddenReview")
    @RequiresPermissions("scyf:hidden:list")
    @ApiOperation(value="检查隐患是否可以评审",notes = "true可以评审,false不可以")
    public R checkedHiddenReview(){
        return R.ok().put("data", hiddenService.checkedHiddenReview(getCompanyId(),getUser().getUserType()));
    }

    /**
     * 查询评审记录
     */
    @GetMapping(value="/getReviewRecord")
    @RequiresPermissions("scyf:hidden:info")
    @ApiOperation(value="查询评审记录",response = HiddenReview.class)
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "path", dataType = "int")
    public R getReviewRecord(Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        return R.ok().put("data", hiddenReviewService.getByHiddenId(hiddenId));
    }

    /**
     * 查询整改记录
     */
    @GetMapping(value="/getRetifyRecord")
    @RequiresPermissions("scyf:hidden:info")
    @ApiOperation(value="查询整改记录",response = HiddenRetify.class)
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "path", dataType = "int")
    public R getRetifyRecord(Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        return R.ok().put("data", hiddenRetifyService.getByHiddenId(hiddenId));
    }

    /**
     * 查询整改验收记录
     */
    @GetMapping(value="/getAcceptRecord")
    @RequiresPermissions("scyf:hidden:info")
    @ApiOperation(value="查询整改验收记录",response = HiddenAccept.class)
    @ApiImplicitParam(name = "hiddenId", value = "隐患ID", required = true, paramType = "path", dataType = "int")
    public R getAcceptRecord(Long hiddenId){
        Assert.isNull(hiddenId,"隐患ID不能为空");
        return R.ok().put("data", hiddenAcceptService.getByHiddenId(hiddenId));
    }
    /**
     * 问题反馈下发为隐患
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hdangerReviewOn", value = "隐患是否有评审的开关，1评审；0不评审", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "feedbackId", value = "问题反馈id", required = true, paramType = "json", dataType = "int"),
           // @ApiImplicitParam(name = "groupId", value = "隐患所属单位", required = true, paramType = "json", dataType = "int") ,
            @ApiImplicitParam(name = "hiddenTitle", value = "隐患名称", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenCatId", value = "隐患类型ID", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLevel", value = "隐患等级", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyGroup", value = "整改部门", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewGroup", value = "评审单位", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenReviewBy", value = "评审人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenQuondamRetifyBy", value = "整改人", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenRetifyDeadline", value = "整改期限", required = false, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenDesc", value = "隐患描述", required = true, paramType = "json", dataType = "string"),
            @ApiImplicitParam(name = "hiddenAcceptedGroup", value = "验收单位", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenAcceptedBy", value = "验收人", required = false, paramType = "json", dataType = "int"),

    })
    @ApiOperation(value="问题反馈下发为隐患 保存", produces="application/json",notes = "" +
            "问题反馈下发接口如下：<br>" +
            "1、查询隐患类型：/sys/common/getCommonClassifyByType?classifyType=1 <br>" +
            "2、查询系统信息  scyf/info/getInfo  <br>"+
            "2、查询隐患等级数据字典：/sys/common/getDictByType?dictType=hidden_level <br>" +
            "4、查询整改部门/评审部门/所属部门列表：/sys/common/getCompanyGroupLists <br>" +
            "5、查询整改人员/评审人员列表：sys/common/getUserByGroupId <br>"
    )
    @PostMapping("/saveFeedbackHiddenDanger")
    public R saveFeedbackHiddenDanger(@RequestBody Hidden hiddenDanger) {
        try {
            if (StringUtils.isNotBlank(hiddenDanger.getHdangerReviewOn().toString()) && Arrays.asList(1, 0).contains(hiddenDanger.getHdangerReviewOn())) {
                if (hiddenDanger.getHdangerReviewOn()==1) {
                    hiddenDanger.setStatus(Constant.DANGER_DPS);
                } else if (hiddenDanger.getHdangerReviewOn()==0) {
                    hiddenDanger.setStatus(Constant.DANGER_DZG);
                }
                hiddenDanger.setCreatedBy(super.getUserId());
                hiddenDanger.setCompanyId(super.getCompanyId());
                hiddenDanger.setCreatedTime(new Date());
                String result = this.hiddenService.saveFeedbackHiddenDanger(hiddenDanger,getUser().getUserType());
                if (StringUtils.isBlank(result)) {
                    return R.ok( "保存成功");
                }else{
                    return R.error(result);
                }
            }
        } catch (Exception var4) {
           var4.printStackTrace();
        }

        return R.error( "保存失败");
    }

    /**
     * 查询隐患台账清单整改台账
     * yfh
     * 2020/06/28
     * @param dataMap
     * @return
     */
    @ApiOperation(value="查询隐患台账清单整改台账")
    @RequestMapping(value = {"/queryRectifyStandingBookByPage"},method = {RequestMethod.GET})
    @ApiImplicitParams({
      @ApiImplicitParam(name = "weighty", value = "0", required = true, paramType = "json", dataType = "int"),
      @ApiImplicitParam(name = "startTime", value = "开始时间", required = false, paramType = "json", dataType = "string"),
      @ApiImplicitParam(name = "endTime", value = "结束时间", required = false, paramType = "json", dataType = "string")
    })
    public R queryRectifyStandingBookByPage(@RequestParam Map<String, Object> dataMap) {
        int pageNum = null!= dataMap.get(Constant.PAGE_PAGENUM) ? Integer.parseInt(dataMap.get(Constant.PAGE_PAGENUM).toString()) : 1;
        int pageSize = null != dataMap.get(Constant.PAGE_PAGESIZE) ? Integer.parseInt(dataMap.get(Constant.PAGE_PAGESIZE).toString()) : Constant.PAGE_SIZE;
        dataMap.put("status", 1);
        dataMap.put("weighty",dataMap.get("weighty"));
        dataMap.put("companyId",getCompanyId());
        PageUtils page = hiddenService.queryRectifyStandingBookByPage(dataMap,pageNum,pageSize,null);
        return R.ok().put("data",page);
    }

    /**
     * 查询隐患台账清单排查清单
     * yfh
     * 2020/06/29
     * @param dataMap
     * @return
     */
    @RequestMapping(value = {"/queryHiddenCheckedListByPage"},method = {RequestMethod.GET})
    public R queryHiddenCheckedListByPage(@RequestParam Map<String, Object> dataMap) {
        dataMap.put("companyId",getCompanyId());
        dataMap.put("positionId",getPositionId());
        List<Map<String,Object>> data= hiddenService.queryHiddenCheckedListByPage(dataMap);
        return R.ok().put("data",data);
    }

    /**
     * 首页隐患通报
     * @param params
     * @return
     */
    @GetMapping("/indexHiddenNotice")
    @ApiOperation(value="首页隐患通报")
    public R indexHiddenNotice(@RequestParam Map<String, Object> params) {
        int pageNum = null!= params.get(Constant.PAGE_PAGENUM) ? Integer.parseInt(params.get(Constant.PAGE_PAGENUM).toString()) : 1;
        int pageSize = null != params.get(Constant.PAGE_PAGESIZE) ? Integer.parseInt(params.get(Constant.PAGE_PAGESIZE).toString()) : Constant.PAGE_SIZE;
        params.put("companyId",getCompanyId());
        params.put("positionId",getPositionId());
        PageUtils page = hiddenService.indexHiddenNotice(params,pageNum,pageSize,"");
        return R.ok().put("data",page);
    }

    /**
     * 首页隐患总计
     * @return
     */
    @GetMapping("/indexHiddenCount")
    @ApiOperation(value="首页隐患总计",notes = "返回结果如下（key为类别；value对应总数量）：<br>" +
            "lv开头，后面对应 1,2,3,4 代表隐患等级：1重大，2较大，3一般，4较低<br>" +
            "s开头，后面对应 2,3,4,5 代表隐患状态：2待整改，3：待验收，4：验收通过，5：验收不通过<br>")
    public R indexHiddenCount() {
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", getCompanyId());
        List<Map<String,String>> list = hiddenService.indexHiddenCount(params);
        return R.ok().put("data",list);
    }

    /**
     * 首页隐患-督导
     */
    @PostMapping("/toSupervise")
    @ApiOperation(value="首页隐患督导")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hiddenOrSchduleIds", value = "隐患/任务id", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenOrSchduleTitle", value = "隐患/任务标题", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "hiddenLvl", value = "隐患等级", required = false, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "superviseGroupId", value = "隐患部门", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "superviseToUserId", value = "执行人", required = true, paramType = "json", dataType = "int"),
            @ApiImplicitParam(name = "superviseType", value = "督导类型 1.隐患  2.任务 ", required = true, paramType = "json", dataType = "int"),
    })
    public R toSupervise(@RequestParam Map<String, Object> params) {
        Assert.isNull(params.get("superviseType"),"督导类型不能为空");
        Assert.isNull(params.get("superviseToUserId"),"执行人不能为空");
        Assert.isNull(params.get("hiddenOrSchduleIds"),"隐患/任务id不能为空");

        params.put("companyId", getCompanyId());
        params.put("currentUser",getUser().getUserName());
        hiddenService.toSupervise(params);
        return R.ok().put("data","督导消息下发成功");
    }

}
