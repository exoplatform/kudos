package org.exoplatform.addon.kudos.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.addon.kudos.model.KudosPeriod;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class KudosDAO extends GenericDAOJPAImpl<KudosEntity, Long> {
  @Override
  public void deleteAll() {
    throw new IllegalStateException("Kudos removal is disabled");
  }

  @Override
  public void delete(KudosEntity entity) {
    throw new IllegalStateException("Kudos removal is disabled");
  }

  @Override
  public void deleteAll(List<KudosEntity> entities) {
    throw new IllegalStateException("Kudos removal is disabled");
  }

  public List<KudosEntity> getAllKudosByPeriod(KudosPeriod kudosPeriod) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getAllKudosByPeriod", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    return query.getResultList();
  }

  public List<KudosEntity> getAllKudosByPeriodAndEntityType(KudosPeriod kudosPeriod, int entityType) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getAllKudosByPeriodAndEntityType", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("entityType", entityType);
    return query.getResultList();
  }

  public List<KudosEntity> getAllKudosByEntity(int entityType, long entityId) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getAllKudosByEntity", KudosEntity.class);
    query.setParameter("entityId", entityId);
    query.setParameter("entityType", entityType);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByPeriodAndReceiver(KudosPeriod kudosPeriod, long receiverId, boolean isReceiverUser) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByPeriodAndReceiver", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("receiverId", receiverId);
    query.setParameter("isReceiverUser", isReceiverUser);
    return query.getResultList();
  }

  public List<KudosEntity> getKudosByPeriodAndSender(KudosPeriod kudosPeriod, long senderId) {
    TypedQuery<KudosEntity> query = getEntityManager().createNamedQuery("Kudos.getKudosByPeriodAndSender", KudosEntity.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("senderId", senderId);
    return query.getResultList();
  }

  public long countKudosByPeriodAndSender(KudosPeriod kudosPeriod, long senderId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Kudos.countKudosByPeriodAndSender", Long.class);
    setPeriodParameters(query, kudosPeriod);
    query.setParameter("senderId", senderId);
    return query.getSingleResult();
  }

  private void setPeriodParameters(TypedQuery<?> query, KudosPeriod kudosPeriod) {
    query.setParameter("startDate", kudosPeriod.getStartDateInSeconds());
    query.setParameter("endDate", kudosPeriod.getEndDateInSeconds());
  }

}
