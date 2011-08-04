package org.zanata.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{

   private final DispatchAsync dispatcher;
   private final WorkspaceContext workspaceContext;

   public interface Display extends WidgetDisplay
   {
      void updateUserList(ArrayList<Person> userList);
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.workspaceContext = workspaceContext;
      this.dispatcher = dispatcher;
      // loadTranslatorList();
   }

   @Override
   protected void onBind()
   {

      final DecoratedPopupPanel userPopupPanel = new DecoratedPopupPanel(true);

      registerHandler(eventBus.addHandler(ExitWorkspaceEvent.getType(), new ExitWorkspaceEventHandler()
      {
         @Override
         public void onExitWorkspace(ExitWorkspaceEvent event)
         {
            loadTranslatorList();
         }
      }));

      registerHandler(eventBus.addHandler(EnterWorkspaceEvent.getType(), new EnterWorkspaceEventHandler()
      {
         @Override
         public void onEnterWorkspace(EnterWorkspaceEvent event)
         {
            loadTranslatorList();
         }
      }));

      // We won't receive the EnterWorkspaceEvent generated by our own login,
      // because
      // this presenter is not bound until we get the callback from
      // EventProcessor.
      // Thus we load the translator list here.
      loadTranslatorList();
   }

   private void loadTranslatorList()
   {
      dispatcher.execute(new GetTranslatorList(), new AsyncCallback<GetTranslatorListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("error fetching translators list: " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetTranslatorListResult result)
         {
            display.updateUserList(result.getTranslatorList());
            // getDisplay().getFilter().setList(result.getTranslatorList());
         }
      });
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

}
