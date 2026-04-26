package com.servalabs.chat.groups;

public final class GroupAlreadyExistsException extends GroupChangeException {

  public GroupAlreadyExistsException(Throwable throwable) {
    super(throwable);
  }
}
