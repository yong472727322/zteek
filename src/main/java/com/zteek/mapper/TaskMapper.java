package com.zteek.mapper;

import com.zteek.entity.AmazonTask;
import com.zteek.entity.AmazonTaskRun;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TaskMapper {

    public List<AmazonTask> getTasks(@Param("maxTaskNum") Integer maxTaskNum);

    public int updateTaskById(@Param("id") Long id);

    int insertTaskRecord(AmazonTaskRun atr);

    int completedTaskById(@Param("taskId") Long taskId);

    int updateTaskRecord(AmazonTaskRun atr);

    /**
     * 首页/ASIN详情页 折线图
     * @return
     */
    List<AmazonTask> lineChart(@Param("resultCode") Integer resultCode,@Param("taskId") Long taskId);

    Map<String,Integer> indexTable(@Param("ct") int ct);

    int insertTask(AmazonTask task);

    List<AmazonTask> getTaskList(Map<String, Object> param);

    /**
     * 查询任务成功率及成功数
     * @param taskId
     * @param rateType  1:今日，2：昨日，3：全部
     * @return
     */
    Map<String,BigDecimal> taskSuccessRate(@Param("taskId") Long taskId, @Param("rateType") int rateType);

    /**
     * 查询成功任务耗时
     * @param taskId
     * @return
     */
    Map<String,Integer> taskConsuming(@Param("taskId") Long taskId);

    /**
     * 根据ID更新任务状态
     * @param dataStatus
     * @param id
     * @return
     */
    int updateTaskStatusById(@Param("dataStatus") int dataStatus, @Param("id") Long id);





    /**
     * PC端获取任务: 1：完成次数<执行次数  2：状态是：运行中
     *      按照  任务级别 降序 ， 创建时间 升序
     * @return
     */
    AmazonTask getTaskForPC();
    /**
     * 更新PC端任务
     * @param id
     * @return
     */
    int updatePCTaskById(Long id);
    /**
     * 添加PC端任务
     * @param task
     * @return
     */
    int insertPCTask(AmazonTaskRun task);
    /**
     * 获取PC端任务列表
     * @param param
     * @return
     */
    List<AmazonTaskRun> getPCTaskList(Map<String, Object> param);
}
