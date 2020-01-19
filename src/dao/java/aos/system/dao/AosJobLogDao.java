package aos.system.dao;

import aos.framework.core.annotation.Dao;
import aos.framework.core.typewrap.Dto;
import aos.system.dao.po.AosJobLogPO;

import java.util.List;

@Dao("aosJobLogDao")
public interface AosJobLogDao {
    List<AosJobLogPO> listPage(Dto qDto);

    int insert(AosJobLogPO sysJobLog);
}
