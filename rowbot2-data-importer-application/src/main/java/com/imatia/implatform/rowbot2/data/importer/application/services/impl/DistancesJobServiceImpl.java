package com.imatia.implatform.rowbot2.data.importer.application.services.impl;

import com.imatia.implatform.rowbot2.data.importer.domain.model.Datasource;
import com.imatia.implatform.rowbot2.data.importer.domain.model.DistancesJob;
import com.imatia.implatform.rowbot2.data.importer.application.services.DatasourceCRUDService;
import com.imatia.implatform.rowbot2.data.importer.application.services.DistancesJobService;
import com.imatia.implatform.rowbot2.data.importer.application.services.base.AbstractCRUDServiceImpl;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.DataEngineJobStatus;
import com.imatia.implatform.rowbot2.data.importer.domain.model.enums.DatasourceStatus;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.entity.DistancesJobDBO;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.IdNotExistentOnDBException;
import com.imatia.implatform.rowbot2.data.importer.domain.model.exception.NotValidStatusException;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.mapper.base.CycleAvoidingMappingContext;
import com.imatia.implatform.rowbot2.data.importer.infrastructure.out.jpa.repository.DistancesJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DistancesJobServiceImpl extends AbstractCRUDServiceImpl<DistancesJob, DistancesJobDBO, DistancesJobRepository> implements DistancesJobService {

	@Autowired
	DatasourceCRUDService datasourceService;

	@Override
	public DistancesJob update(DistancesJob job){
		DistancesJobDBO oldSavedJob = repo.findByJobId(job.getJobId());
		if(oldSavedJob==null){
			throw new IdNotExistentOnDBException("The job "+job.getJobId()+" is not present in our DB, it cannot be updated");
		}
		Datasource datasource = datasourceService.read(oldSavedJob.getDatasourceId())
				.orElseThrow(()-> new IdNotExistentOnDBException("There is no datasource in our DB related with a job with id: "+job.getJobId()));
		if(!datasource.getStatus().equals(DatasourceStatus.CALCULATING_DISTANCE.getDescription())){
			throw new NotValidStatusException("The datasource with id:"+datasource.getId()+" ,related with the data engine job "+job.getJobId()+" has not a valid status. " +
					"Expected: "+DatasourceStatus.CALCULATING_DISTANCE.getDescription() +
					". Current: "+datasource.getStatus());
		}
		if(job.getJobStatus().equals(DataEngineJobStatus.COMPLETED.getDescription())){
			datasourceService.updateStatus(oldSavedJob.getDatasourceId(), DatasourceStatus.READY.getDescription());
		}
		if(job.getJobStatus().equals(DataEngineJobStatus.FAILED.getDescription())){
			datasourceService.updateStatus(oldSavedJob.getDatasourceId(), DatasourceStatus.ERROR.getDescription(), job.getErrorMsg());
		}

		DistancesJobDBO savedJob = repo.save(
				oldSavedJob.toBuilder()
						.jobStatus(job.getJobStatus())
						.errorMsg(job.getErrorMsg())
					.build());
		return detailMapper.fromDBO(savedJob);
	}

	@Override
	public void deleteByDatasourceId(Long datasourceId){
		repo.deleteByDatasourceId(datasourceId);
	}

}
