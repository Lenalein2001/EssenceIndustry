package com.neolena.essenceindustry.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.*;

/** Six fixed keys, exact long quantities, and constant-size transaction snapshots. */
public final class EssenceStorage implements ResourceHandler<ItemResource> {
    public record Entry(ItemStack item, long count) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(Entry::item),
                Codec.LONG.fieldOf("count").forGetter(Entry::count)).apply(i, Entry::new));
    }
    private final ItemResource[] resources = new ItemResource[6];
    private final long[] amounts = new long[6];
    private final Runnable changed;
    private record State(ItemResource[] resources, long[] amounts) {}
    private final SnapshotJournal<State> journal = new SnapshotJournal<>() {
        protected State createSnapshot() { return new State(resources.clone(), amounts.clone()); }
        protected void revertToSnapshot(State state) { System.arraycopy(state.resources,0,resources,0,6);System.arraycopy(state.amounts,0,amounts,0,6); }
        protected void onRootCommit(State state) { if (!Arrays.equals(state.amounts,amounts) || !Arrays.equals(state.resources,resources)) changed.run(); }
    };
    public EssenceStorage(Runnable changed) { this.changed=changed;Arrays.fill(resources,ItemResource.EMPTY); }
    public int size() { return 6; }
    public ItemResource getResource(int i) { return resources[i]; }
    public long getAmountAsLong(int i) { return amounts[i]; }
    public long getCapacityAsLong(int i,ItemResource resource) { return resource.isEmpty() || isValid(i,resource) ? Long.MAX_VALUE : 0; }
    public boolean isValid(int i,ItemResource resource) { return i>=0 && i<6 && !resource.isEmpty() && EssenceConverterTile.essence(i)!=net.minecraft.world.item.Items.AIR && resource.equals(ItemResource.of(EssenceConverterTile.essence(i))); }
    public int insert(int i,ItemResource resource,int amount,TransactionContext tx) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        return (int)insertLong(i,resource,amount,tx);
    }
    public long insertLong(int i,ItemResource resource,long amount,TransactionContext tx) {
        if(resource.isEmpty() || amount<0)throw new IllegalArgumentException("Invalid essence transfer");
        if(!isValid(i,resource) || (!resources[i].isEmpty() && !resources[i].equals(resource)))return 0;
        long moved=Math.min(amount,Long.MAX_VALUE-amounts[i]);
        if(moved>0){journal.updateSnapshots(tx);resources[i]=resource;amounts[i]+=moved;}return moved;
    }
    public int extract(int i,ItemResource resource,int amount,TransactionContext tx) {
        TransferPreconditions.checkNonEmptyNonNegative(resource,amount);
        return (int)extractLong(i,resource,amount,tx);
    }
    public long extractLong(int i,ItemResource resource,long amount,TransactionContext tx) {
        if(resource.isEmpty() || amount<0)throw new IllegalArgumentException("Invalid essence transfer");
        if(!resources[i].equals(resource))return 0;
        long moved=Math.min(amount,amounts[i]);
        if(moved>0){journal.updateSnapshots(tx);amounts[i]-=moved;if(amounts[i]==0)resources[i]=ItemResource.EMPTY;}return moved;
    }
    public void set(int i,ItemResource resource,long amount) {
        if(amount<0 || resource.isEmpty() && amount!=0)throw new IllegalArgumentException("Invalid essence quantity");
        resources[i]=amount==0?ItemResource.EMPTY:resource;amounts[i]=amount;changed.run();
    }
    public List<Entry> entries() { var out=new ArrayList<Entry>(6);for(int i=0;i<6;i++)out.add(new Entry(resources[i].toStack(),amounts[i]));return out; }
    public void load(List<Entry> entries) {
        if(entries.size()!=6)throw new IllegalArgumentException("Expected six essence tiers");
        for(int i=0;i<6;i++){var e=entries.get(i);if(e.count<0 || e.item.isEmpty()!=(e.count==0) || !e.item.isEmpty() && (e.item.getCount()!=1 || !isValid(i,ItemResource.of(e.item))))throw new IllegalArgumentException("Invalid essence record");}
        for(int i=0;i<6;i++){resources[i]=ItemResource.of(entries.get(i).item);amounts[i]=entries.get(i).count;}changed.run();
    }
}
