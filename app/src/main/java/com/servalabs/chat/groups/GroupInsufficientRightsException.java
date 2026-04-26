package com.servalabs.chat.groups;

public final class GroupInsufficientRightsException extends GroupChangeException {

  public GroupInsufficientRightsException(Throwable throwable) {
    super(throwable);
  }
}
