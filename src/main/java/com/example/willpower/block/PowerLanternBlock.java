if (player.isSneaking()) {
            var item = ModCorpsRegistry.LANTERNS.get(corps);
            if (item != null) player.getInventory().offerOrDrop(new ItemStack(item));
            world.removeBlock(pos, false);
            world.playSound(null, pos, SoundEvents.BLOCK_METAL_BREAK, SoundCategory.BLOCKS, 0.8f, 1.2f);
            return ActionResult.CONSUME;
        }
