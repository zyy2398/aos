package aos.system.dao;

import aos.framework.core.annotation.Dao;
import aos.framework.core.typewrap.Dto;
import aos.system.dao.po.AosJobPO;

import java.util.List;

@Dao("aosJobDao")
public interface AosJobDao {
    List<AosJobPO> listPage(Dto inDto);

    int insert(AosJobPO aosJobPO);

    int update(AosJobPO aosJobPO);

    int deleteByKey(String job_id);

    AosJobPO selectJobById(Long job_id);
}
