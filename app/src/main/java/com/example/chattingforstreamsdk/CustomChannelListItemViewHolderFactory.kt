package com.example.chattingforstreamsdk

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.chattingforstreamsdk.databinding.CustomChannelListItemBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory

class CustomChannelListItemViewHolderFactory : ChannelListItemViewHolderFactory() {
    override fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
        return CustomChannelViewHolder(parentView, listenerContainer.channelClickListener)
    }
}

class CustomChannelViewHolder(
    parent: ViewGroup,
    private val channelClickListener: ChannelListView.ChannelClickListener,
    private val binding: CustomChannelListItemBinding = CustomChannelListItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    ),
) : BaseChannelListItemViewHolder(binding.root) {

    private lateinit var channel: Channel

    init {
        binding.root.setOnClickListener { channelClickListener.onClick(channel) }
    }

    override fun bind(channel: Channel, diff: ChannelListPayloadDiff) {
        this.channel = channel

        binding.apply {
            avatarView.setChannelData(channel)

            nameTextView.text = ChatUI.channelNameFormatter.formatChannelName(
                channel = channel,
                currentUser = ChatClient.instance().getCurrentUser()
            )
            membersCountTextView.text = itemView.context.resources.getQuantityString(
                R.plurals.members_count,
                channel.members.size,
                channel.members.size
            )
            val cal = Calendar.getInstance().time.time
            var updateAt = 0L
            channel.lastMessageAt?.time?.let {
                updateAt = it
            }
            val time =  (cal - updateAt) / (24 * 60 * 60 * 1000)
            tvDate.text = binding.root.context.getString(R.string.time, time)
        }
    }
}
