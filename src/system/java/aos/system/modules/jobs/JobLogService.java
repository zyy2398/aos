package aos.system.modules.jobs;

import aos.framework.core.typewrap.Dto;
import aos.framework.core.utils.AOSJson;
import aos.framework.web.router.HttpModel;
import aos.system.common.id.IdService;
import aos.system.common.utils.SystemCons;
import aos.system.dao.AosJobLogDao;
import aos.system.dao.po.AosJobLogPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobLogService {

    @Autowired
    private AosJobLogDao aosJobLogDao;
    @Autowired
    private IdService idService;

    public void listJobLog(HttpModel httpModel){
        Dto qDto = httpModel.getInDto();
        List<AosJobLogPO> list = aosJobLogDao.listPage(qDto);
        httpModel.setOutMsg(AOSJson.toGridJson(list,qDto.getPageTotal()));
    }

    @Transactional
    public void addJobLog(AosJobLogPO sysJobLog) {
        sysJobLog.setJob_log_id(idService.nextValue(SystemCons.SEQ.SEQ_SYSTEM).longValue());
        aosJobLogDao.insert(sysJobLog);
    }
}
