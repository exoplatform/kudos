package org.exoplatform.addon.kudos.service.utils;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.kudos.entity.KudosEntity;
import org.exoplatform.addon.kudos.model.Kudos;
import org.exoplatform.addon.kudos.model.KudosEntityType;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.LinkProviderUtils;

public class Utils {

  public static final String                 SPACE_ACCOUNT_TYPE                      = "space";

  public static final String                 USER_ACCOUNT_TYPE                       = "user";

  public static final String                 DEFAULT_ACCESS_PERMISSION               = "defaultAccessPermission";

  public static final String                 DEFAULT_KUDOS_PER_MONTH                 = "defaultKudosPerMonth";

  public static final String                 KUDOS_ACTIVITY_RECEIVER_NOTIFICATION_ID = "KudosActivityReceiverNotificationPlugin";

  public static final String                 KUDOS_SENT_EVENT                        = "exo.addons.kudos.sent";

  public static final String                 KUDOS_ACTIVITY_COMMENT_TYPE             = "exokudos:activity";

  public static final String                 KUDOS_ACTIVITY_COMMENT_TITLE_ID         = "activity_kudos";

  public final static String                 RESOURCE_BUNDLE_VALUES_PARAM            = "RESOURCE_BUNDLE_VALUES_PARAM";

  public final static String                 RESOURCE_BUNDLE_KEY_TO_PROCESS          = "RESOURCE_BUNDLE_KEY_TO_PROCESS";

  public final static String                 RESOURCE_BUNDLE_ESCAPE_CHARACTER        = "${_}";

  public final static String                 RESOURCE_BUNDLE_VALUES_CHARACTER        = "#";

  public static final ArgumentLiteral<Kudos> KUDOS_ACTIVITY_DETAILS_PARAMETER        =
                                                                              new ArgumentLiteral<>(Kudos.class, "kudos");

  public static Space getSpace(String id) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (id.indexOf("/spaces/") >= 0) {
      return spaceService.getSpaceByGroupId(id);
    }
    Space space = spaceService.getSpaceByPrettyName(id);
    if (space == null) {
      space = spaceService.getSpaceByGroupId("/spaces/" + id);
      if (space == null) {
        space = spaceService.getSpaceByDisplayName(id);
        if (space == null) {
          space = spaceService.getSpaceByUrl(id);
        }
      }
    }
    return space;
  }

  public static final String getCurrentUserId() {
    if (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null;
  }

  public static List<String> getNotificationReceiversUsers(String receiverType, String receiverId, String senderId) {
    if (SPACE_ACCOUNT_TYPE.equals(receiverType) || SpaceIdentityProvider.NAME.equals(receiverType)) {
      Space space = getSpace(receiverId);
      if (space == null) {
        return Collections.singletonList(receiverId);
      } else {
        String[] members = space.getMembers();
        if (members == null || members.length == 0) {
          return Collections.emptyList();
        } else if (StringUtils.isBlank(senderId)) {
          return Arrays.asList(members);
        } else {
          return Arrays.stream(members).filter(member -> !senderId.equals(member)).collect(Collectors.toList());
        }
      }
    } else {
      return Collections.singletonList(receiverId);
    }
  }

  public static String getReceiverIdentityProviderType(String receiverType) {
    return SpaceIdentityProvider.NAME.equals(receiverType)
        || SPACE_ACCOUNT_TYPE.equals(receiverType) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
  }

  public static String getReceiverType(String receiverType) {
    return SpaceIdentityProvider.NAME.equals(receiverType) || SPACE_ACCOUNT_TYPE.equals(receiverType) ? SPACE_ACCOUNT_TYPE
                                                                                                      : USER_ACCOUNT_TYPE;
  }

  public static Kudos fromEntity(KudosEntity kudosEntity) {
    Kudos kudos = new Kudos();
    kudos.setTechnicalId(kudosEntity.getId());
    kudos.setMessage(kudosEntity.getMessage());
    kudos.setEntityId(String.valueOf(kudosEntity.getEntityId()));

    kudos.setEntityType(KudosEntityType.values()[kudosEntity.getEntityType()].name());
    Identity receiverIdentity = getIdentityById(kudosEntity.getReceiverId());
    kudos.setReceiverId(receiverIdentity.getRemoteId());
    kudos.setReceiverType(kudosEntity.isReceiverUser() ? USER_ACCOUNT_TYPE : SPACE_ACCOUNT_TYPE);
    if (kudosEntity.isReceiverUser()) {
      kudos.setReceiverFullName(receiverIdentity.getProfile().getFullName());
      kudos.setReceiverURL(LinkProviderUtils.getRedirectUrl(kudos.getReceiverType(), receiverIdentity.getRemoteId()));
    } else {
      Space space = getSpace(receiverIdentity.getRemoteId());
      kudos.setReceiverFullName(space.getDisplayName());
      kudos.setReceiverURL(LinkProviderUtils.getRedirectUrl(kudos.getReceiverType(), space.getId()));
    }
    Identity senderIdentity = getIdentityById(kudosEntity.getSenderId());
    kudos.setSenderId(senderIdentity.getRemoteId());
    kudos.setTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(kudosEntity.getCreatedDate()), TimeZone.getDefault().toZoneId()));
    return kudos;
  }

  public static KudosEntity toNewEntity(Kudos kudos) {
    KudosEntity kudosEntity = new KudosEntity();
    kudosEntity.setMessage(kudos.getMessage());
    kudosEntity.setEntityId(Long.parseLong(kudos.getEntityId()));
    kudosEntity.setEntityType(KudosEntityType.valueOf(kudos.getEntityType()).ordinal());
    kudosEntity.setSenderId(Long.parseLong(getIdentity(OrganizationIdentityProvider.NAME, kudos.getSenderId()).getId()));

    boolean isReceiverUser = OrganizationIdentityProvider.NAME.equals(kudos.getReceiverType())
        || USER_ACCOUNT_TYPE.equals(kudos.getReceiverType());
    kudosEntity.setReceiverUser(isReceiverUser);
    kudosEntity.setReceiverId(Long.parseLong(getIdentity(isReceiverUser ? OrganizationIdentityProvider.NAME
                                                                        : SpaceIdentityProvider.NAME,
                                                         kudos.getReceiverId()).getId()));
    kudosEntity.setCreatedDate(kudos.getTime().atZone(ZoneOffset.systemDefault()).toEpochSecond());
    return kudosEntity;
  }

  @SuppressWarnings("deprecation")
  private static Identity getIdentityById(long identityId) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getIdentity(String.valueOf(identityId));
  }

  private static Identity getIdentity(String providerId, String remoteId) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getOrCreateIdentity(providerId, remoteId, true);
  }

}
