/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.service;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitSaveService
{
   private final TableEditorMessages messages;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final Provider<UndoLink> undoLinkProvider;

   @Inject
   public TransUnitSaveService(TableEditorMessages messages, EventBus eventBus, CachingDispatchAsync dispatcher, Provider<UndoLink> undoLinkProvider)
   {
      this.messages = messages;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.undoLinkProvider = undoLinkProvider;
   }

   public void saveTranslation(final TransUnit old, List<String> newTargets, ContentState status, final SaveResultCallback callback)
   {
      TransUnitUpdated.UpdateType updateType = status == ContentState.Approved ? TransUnitUpdated.UpdateType.WebEditorSave : TransUnitUpdated.UpdateType.WebEditorSaveFuzzy;
      final UpdateTransUnit updateTransUnit = new UpdateTransUnit(new TransUnitUpdateRequest(old.getId(), newTargets, status, old.getVerNum()), updateType);
      Log.debug("about to save translation: " + updateTransUnit);
      dispatcher.execute(updateTransUnit, new AsyncCallback<UpdateTransUnitResult>()
      {
         @Override
         public void onFailure(Throwable e)
         {
            Log.error("UpdateTransUnit failure ", e);
            String message = e.getLocalizedMessage();
            saveFailure(message, callback);
         }

         @Override
         public void onSuccess(UpdateTransUnitResult result)
         {
            // FIXME check result.success
            TransUnit updatedTU = result.getUpdateInfoList().get(0).getTransUnit();
            Log.debug("save resulted TU: " + updatedTU.debugString());
            if (result.isSingleSuccess())
            {
               UndoLink undoLink = undoLinkProvider.get();
               undoLink.prepareUndoFor(result);
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyUpdateSaved(), undoLink));
               callback.onSaveSuccess(updatedTU, undoLink);
            }
            else
            {
               // TODO localised message
               saveFailure("row " + old.getRowIndex(), callback);
            }
         }
      });
   }

   private void saveFailure(String message, SaveResultCallback callback)
   {
      eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyUpdateFailed(message)));
      callback.onSaveFail();
   }

   public static interface SaveResultCallback
   {
      void onSaveSuccess(TransUnit updatedTU, UndoLink undoLink);
      void onSaveFail();
   }
}
