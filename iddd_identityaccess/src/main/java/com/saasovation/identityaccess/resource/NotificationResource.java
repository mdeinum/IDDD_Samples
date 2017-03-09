//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.identityaccess.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.saasovation.common.media.Link;
import com.saasovation.common.media.OvationsMediaType;
import com.saasovation.common.notification.NotificationLog;
import com.saasovation.common.serializer.ObjectSerializer;
import com.saasovation.identityaccess.application.representation.NotificationLogRepresentation;

@RestController
@RequestMapping("/notifications")
public class NotificationResource extends AbstractResource {

    public NotificationResource() {
        super();
    }

    @GetMapping(produces = OvationsMediaType.ID_OVATION_TYPE)
    public ResponseEntity<String> getCurrentNotificationLog() {

        NotificationLog currentNotificationLog = this.notificationApplicationService().currentNotificationLog();

        if (currentNotificationLog == null) {
            return ResponseEntity.notFound().build();
        }

        return this.currentNotificationLogResponse(currentNotificationLog);

    }

    @GetMapping(value = "{notificationId}", produces = OvationsMediaType.ID_OVATION_TYPE)
    public ResponseEntity<String> getNotificationLog(@PathVariable("notificationId") String aNotificationId) {

        NotificationLog notificationLog = this.notificationApplicationService().notificationLog(aNotificationId);

        if (notificationLog == null) {
            return ResponseEntity.notFound().build();
        }

        return this.notificationLogResponse(notificationLog);

    }

    private ResponseEntity<String> currentNotificationLogResponse(NotificationLog aCurrentNotificationLog) {

        NotificationLogRepresentation log = new NotificationLogRepresentation(aCurrentNotificationLog);

        log.setLinkSelf(this.selfLink(aCurrentNotificationLog));

        log.setLinkPrevious(this.previousLink(aCurrentNotificationLog));

        String serializedLog = ObjectSerializer.instance().serialize(log);

        return ResponseEntity.ok().cacheControl(this.cacheControlFor(60)).body(serializedLog);

    }

    private ResponseEntity<String> notificationLogResponse(NotificationLog aNotificationLog) {

        NotificationLogRepresentation log = new NotificationLogRepresentation(aNotificationLog);

        log.setLinkSelf(this.selfLink(aNotificationLog));

        log.setLinkNext(this.nextLink(aNotificationLog));

        log.setLinkPrevious(this.previousLink(aNotificationLog));

        String serializedLog = ObjectSerializer.instance().serialize(log);

        return ResponseEntity.ok().cacheControl(this.cacheControlFor(3600)).body(serializedLog);
    }

    private Link linkFor(String aRelationship, String anId) {

        Link link = null;

        if (anId != null) {

            UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/notifications/{notificationId}");

            String linkUrl = builder.buildAndExpand(anId).toUriString();

            link = new Link(linkUrl, aRelationship, null, OvationsMediaType.ID_OVATION_TYPE);
        }

        return link;
    }

    private Link nextLink(NotificationLog aNotificationLog) {
        return this.linkFor("next", aNotificationLog.nextNotificationLogId());
    }

    private Link previousLink(NotificationLog aNotificationLog) {

        return this.linkFor("previous", aNotificationLog.previousNotificationLogId());
    }

    private Link selfLink(NotificationLog aNotificationLog) {
        return this.linkFor("self", aNotificationLog.notificationLogId());
    }
}
