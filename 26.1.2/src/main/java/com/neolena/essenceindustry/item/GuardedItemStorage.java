package com.neolena.essenceindustry.item;

import java.util.function.BooleanSupplier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** A retained capability must not mutate an inventory after its owning block leaves the world. */
public final class GuardedItemStorage implements ResourceHandler<ItemResource> {
    private final ResourceHandler<ItemResource> delegate;
    private final BooleanSupplier available;
    public GuardedItemStorage(ResourceHandler<ItemResource> delegate, BooleanSupplier available) { this.delegate = delegate; this.available = available; }
    @Override public int size() { return available.getAsBoolean() ? delegate.size() : 0; }
    @Override public ItemResource getResource(int index) { return available.getAsBoolean() ? delegate.getResource(index) : ItemResource.EMPTY; }
    @Override public long getAmountAsLong(int index) { return available.getAsBoolean() ? delegate.getAmountAsLong(index) : 0; }
    @Override public long getCapacityAsLong(int index, ItemResource resource) { return available.getAsBoolean() ? delegate.getCapacityAsLong(index, resource) : 0; }
    @Override public boolean isValid(int index, ItemResource resource) { return available.getAsBoolean() && delegate.isValid(index, resource); }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext tx) { return available.getAsBoolean() ? delegate.insert(index, resource, amount, tx) : 0; }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext tx) { return available.getAsBoolean() ? delegate.extract(index, resource, amount, tx) : 0; }
    @Override public int insert(ItemResource resource, int amount, TransactionContext tx) { return available.getAsBoolean() ? delegate.insert(resource, amount, tx) : 0; }
    @Override public int extract(ItemResource resource, int amount, TransactionContext tx) { return available.getAsBoolean() ? delegate.extract(resource, amount, tx) : 0; }
}
