package com.servalabs.chat;

import android.net.Uri;
import android.view.GestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.bumptech.glide.RequestManager;

import org.signal.ringrtc.CallLinkRootKey;
import com.servalabs.chat.components.voice.VoiceNotePlaybackState;
import com.servalabs.chat.contactshare.Contact;
import com.servalabs.chat.conversation.ConversationItem;
import com.servalabs.chat.conversation.ConversationItemDisplayMode;
import com.servalabs.chat.conversation.ConversationMessage;
import com.servalabs.chat.conversation.colors.Colorizable;
import com.servalabs.chat.conversation.colors.Colorizer;
import com.servalabs.chat.conversation.mutiselect.MultiselectPart;
import com.servalabs.chat.conversation.mutiselect.Multiselectable;
import com.servalabs.chat.database.model.InMemoryMessageRecord;
import com.servalabs.chat.database.model.MessageRecord;
import com.servalabs.chat.database.model.MmsMessageRecord;
import com.servalabs.chat.giph.mp4.GiphyMp4Playable;
import com.servalabs.chat.groups.GroupId;
import com.servalabs.chat.groups.GroupMigrationMembershipChange;
import com.servalabs.chat.linkpreview.LinkPreview;
import com.servalabs.chat.mediapreview.MediaIntentFactory;
import com.servalabs.chat.polls.PollOption;
import com.servalabs.chat.polls.PollRecord;
import com.servalabs.chat.recipients.Recipient;
import com.servalabs.chat.recipients.RecipientId;
import com.servalabs.chat.stickers.StickerLocator;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public interface BindableConversationItem extends Unbindable, GiphyMp4Playable, Colorizable, Multiselectable {
  void bind(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull ConversationMessage messageRecord,
            @NonNull Optional<MessageRecord> previousMessageRecord,
            @NonNull Optional<MessageRecord> nextMessageRecord,
            @NonNull RequestManager requestManager,
            @NonNull Locale locale,
            @NonNull Set<MultiselectPart> batchSelected,
            @NonNull Recipient recipients,
            @Nullable String searchQuery,
            boolean pulseMention,
            boolean hasWallpaper,
            boolean isMessageRequestAccepted,
            boolean canPlayInline,
            @NonNull Colorizer colorizer,
            @NonNull ConversationItemDisplayMode displayMode);

  @NonNull ConversationMessage getConversationMessage();

  void setEventListener(@Nullable EventListener listener);

  default void setGestureDetector(@Nullable GestureDetector gestureDetector) {
    // Intentionally Blank.
  }

  default void setParentScrolling(boolean isParentScrolling) {
    // Intentionally Blank.
  }

  default void updateTimestamps() {
    // Intentionally Blank.
  }

  default void updateContactNameColor() {
    // Intentionally Blank.
  }

  default void updateSelectedState() {
    // Intentionally Blank.
  }

  interface EventListener {
    void onQuoteClicked(MmsMessageRecord messageRecord);
    void onLinkPreviewClicked(@NonNull LinkPreview linkPreview);
    void onQuotedIndicatorClicked(@NonNull MessageRecord messageRecord);
    void onMoreTextClicked(@NonNull RecipientId conversationRecipientId, long messageId, boolean isMms);
    void onStickerClicked(@NonNull StickerLocator stickerLocator);
    void onViewOnceMessageClicked(@NonNull MmsMessageRecord messageRecord);
    void onSharedContactDetailsClicked(@NonNull Contact contact, @NonNull View avatarTransitionView);
    void onAddToContactsClicked(@NonNull Contact contact);
    void onMessageSharedContactClicked(@NonNull List<Recipient> choices);
    void onInviteSharedContactClicked(@NonNull List<Recipient> choices);
    void onReactionClicked(@NonNull MultiselectPart multiselectPart, long messageId, boolean isMms);
    void onGroupMemberClicked(@NonNull RecipientId recipientId, @NonNull GroupId groupId);
    void onMessageWithErrorClicked(@NonNull MessageRecord messageRecord);
    void onMessageWithRecaptchaNeededClicked(@NonNull MessageRecord messageRecord);
    void onIncomingIdentityMismatchClicked(@NonNull RecipientId recipientId);
    void onRegisterVoiceNoteCallbacks(@NonNull Observer<VoiceNotePlaybackState> onPlaybackStartObserver);
    void onUnregisterVoiceNoteCallbacks(@NonNull Observer<VoiceNotePlaybackState> onPlaybackStartObserver);
    void onVoiceNotePause(@NonNull Uri uri);
    void onVoiceNotePlay(@NonNull Uri uri, long messageId, double position);

    default void onSingleVoiceNotePlay(@NonNull Uri uri, long messageId, double position) {
      onVoiceNotePlay(uri, messageId, position);
    }

    void onVoiceNoteSeekTo(@NonNull Uri uri, double position);
    void onVoiceNotePlaybackSpeedChanged(@NonNull Uri uri, float speed);
    void onGroupMigrationLearnMoreClicked(@NonNull GroupMigrationMembershipChange membershipChange);
    void onChatSessionRefreshLearnMoreClicked();
    void onBadDecryptLearnMoreClicked(@NonNull RecipientId author);
    void onSafetyNumberLearnMoreClicked(@NonNull Recipient recipient);
    void onJoinGroupCallClicked();
    void onInviteFriendsToGroupClicked(@NonNull GroupId.V2 groupId);
    void onEnableCallNotificationsClicked();
    void onPlayInlineContent(ConversationMessage conversationMessage);
    void onInMemoryMessageClicked(@NonNull InMemoryMessageRecord messageRecord);
    void onViewGroupDescriptionChange(@Nullable GroupId groupId, @NonNull String description, boolean isMessageRequestAccepted);
    void onChangeNumberUpdateContact(@NonNull Recipient recipient);
    void onChangeProfileNameUpdateContact(@NonNull Recipient recipient);
    void onCallToAction(@NonNull String action);
    void onDonateClicked();
    void onBlockJoinRequest(@NonNull Recipient recipient);
    void onRecipientNameClicked(@NonNull RecipientId target);
    void onInviteToSignalClicked();
    void onActivatePaymentsClicked();
    void onSendPaymentClicked(@NonNull RecipientId recipientId);
    void onScheduledIndicatorClicked(@NonNull View view, @NonNull ConversationMessage conversationMessage);
    /** @return true if handled, false if you want to let the normal url handling continue */
    boolean onUrlClicked(@NonNull String url);
    void onViewGiftBadgeClicked(@NonNull MessageRecord messageRecord);
    void onGiftBadgeRevealed(@NonNull MessageRecord messageRecord);
    void goToMediaPreview(ConversationItem parent, View sharedElement, MediaIntentFactory.MediaPreviewArgs args);
    void onEditedIndicatorClicked(@NonNull ConversationMessage conversationMessage);
    void onShowGroupDescriptionClicked(@NonNull String groupName, @NonNull String description, boolean shouldLinkifyWebLinks);
    void onJoinCallLink(@NonNull CallLinkRootKey callLinkRootKey);
    void onShowSafetyTips(boolean forGroup);
    void onReportSpamLearnMoreClicked();
    void onMessageRequestAcceptOptionsClicked();
    void onItemDoubleClick(MultiselectPart multiselectPart);
    void onPaymentTombstoneClicked();
    void onDisplayMediaNoLongerAvailableSheet();
    void onShowUnverifiedProfileSheet(boolean forGroup);
    void onUpdateSignalClicked();
    void onViewResultsClicked(long pollId);
    void onViewPollClicked(long messageId);
    void onToggleVote(@NonNull PollRecord poll, @NonNull PollOption pollOption, Boolean isChecked);
    void onViewPinnedMessage(long messageId);
    void onExpandEvents(long messageId, @NonNull View itemView, int collapsedSize);
    void onCollapseEvents(long messageId, @NonNull View itemView, int collapsedSize);
  }
}
