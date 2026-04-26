package com.servalabs.chat.groups.ui.chooseadmin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.util.stream.Collectors;

import com.servalabs.chat.MainActivity;
import com.servalabs.chat.PassphraseRequiredActivity;
import com.servalabs.chat.R;
import com.servalabs.chat.groups.BadGroupIdException;
import com.servalabs.chat.groups.GroupId;
import com.servalabs.chat.groups.ui.GroupChangeResult;
import com.servalabs.chat.groups.ui.GroupErrors;
import com.servalabs.chat.groups.ui.GroupMemberEntry;
import com.servalabs.chat.groups.ui.GroupMemberListView;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.util.DynamicNoActionBarTheme;
import com.servalabs.chat.util.DynamicTheme;
import com.servalabs.chat.util.views.CircularProgressMaterialButton;

import java.util.Objects;

public final class ChooseNewAdminActivity extends PassphraseRequiredActivity {

  private static final String EXTRA_GROUP_ID = "group_id";

  private ChooseNewAdminViewModel        viewModel;
  private GroupMemberListView            groupList;
  private CircularProgressMaterialButton done;
  private GroupId.V2                     groupId;

  private final DynamicTheme dynamicTheme = new DynamicNoActionBarTheme();

  public static Intent createIntent(@NonNull Context context, @NonNull GroupId.V2 groupId) {
    Intent intent = new Intent(context, ChooseNewAdminActivity.class);
    intent.putExtra(EXTRA_GROUP_ID, groupId.toString());
    return intent;
  }

  @Override
  protected void onPreCreate() {
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    super.onCreate(savedInstanceState, ready);
    setContentView(R.layout.choose_new_admin_activity);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    //noinspection ConstantConditions
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    try {
      groupId = GroupId.parse(Objects.requireNonNull(getIntent().getStringExtra(EXTRA_GROUP_ID))).requireV2();
    } catch (BadGroupIdException e) {
      throw new AssertionError(e);
    }

    groupList = findViewById(R.id.choose_new_admin_group_list);
    done      = findViewById(R.id.choose_new_admin_done);

    initializeViewModel();

    groupList.initializeAdapter(this);
    groupList.setRecipientSelectionChangeListener(selection -> viewModel.setSelection(selection.stream()
                                                                                               .filter(x -> x instanceof GroupMemberEntry.FullMember)
                                                                                               .map(x-> (GroupMemberEntry.FullMember)x)
                                                                                               .collect(Collectors.toSet())));

    done.setOnClickListener(v -> {
      done.setSpinning();
      viewModel.updateAdminsAndLeave(this::handleUpdateAndLeaveResult);
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    dynamicTheme.onResume(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void initializeViewModel() {
    viewModel = new ViewModelProvider(this, new ChooseNewAdminViewModel.Factory(groupId)).get(ChooseNewAdminViewModel.class);

    viewModel.getNonAdminFullMembers().observe(this, groupList::setMembers);
    viewModel.getSelection().observe(this, selection -> done.setVisibility(selection.isEmpty() ? View.GONE : View.VISIBLE));
  }

  private void handleUpdateAndLeaveResult(@NonNull GroupChangeResult updateResult) {
    if (updateResult.isSuccess()) {
      String title = Recipient.externalGroupExact(groupId).getDisplayName(this);
      Toast.makeText(this, getString(R.string.ChooseNewAdminActivity_you_left, title), Toast.LENGTH_LONG).show();
      startActivity(MainActivity.clearTop(this));
      finish();
    } else {
      done.cancelSpinning();
      //noinspection ConstantConditions
      Toast.makeText(this, GroupErrors.getUserDisplayMessage(updateResult.getFailureReason()), Toast.LENGTH_LONG).show();
    }
  }
}
