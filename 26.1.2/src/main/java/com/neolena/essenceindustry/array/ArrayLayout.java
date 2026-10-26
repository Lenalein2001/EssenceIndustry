package com.neolena.essenceindustry.array;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.block.Block;
/** One coordinate system for validation, rendering, ports, bounds and the build guide. */
public final class ArrayLayout {
 public static final float HEAD_SCALE=.35f;
 public static double headY(int height,double localY){return height-1.2+(localY-3.8)*HEAD_SCALE;}
 public static final EnumProperty<Direction> FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final BooleanProperty FORMED=BooleanProperty.create("array_formed");
 public static final IntegerProperty PART=IntegerProperty.create("array_part",0,6);
 public static Direction facing(BlockState state){return state.hasProperty(FACING)?state.getValue(FACING):state.hasProperty(BlockStateProperties.FACING)?state.getValue(BlockStateProperties.FACING):Direction.NORTH;}
 public static Direction surface(Direction face,int x,int y,int z,int w,int d){return surface(face,x,y,z,w,d,5);}
 public static BlockPos position(BlockPos controller,Direction face,int x,int y,int z){var right=face.getClockWise();var back=face.getOpposite();return controller.below().offset(right.getStepX()*x+back.getStepX()*z,y,right.getStepZ()*x+back.getStepZ()*z);}
 public static BlockPos local(BlockPos controller,Direction face,BlockPos pos){var delta=pos.subtract(controller.below());var right=face.getClockWise();var back=face.getOpposite();return new BlockPos(delta.getX()*right.getStepX()+delta.getZ()*right.getStepZ(),delta.getY(),delta.getX()*back.getStepX()+delta.getZ()*back.getStepZ());}
 public static Direction surface(Direction face,int x,int y,int z,int w,int d,int h){return y==0?Direction.DOWN:y==h-1?Direction.UP:outward(face,x,y,z,w,d);}
 public static boolean shell(int x,int y,int z,int w,int d,int h){return y==0||y==h-1||x==0||z==0||x==w-1||z==d-1;}
 public static boolean socket(int x,int y,int z,int w,int d,int h){return y>=1&&y<=h-3&&x>=2&&x<=w-3&&z>=2&&z<=d-3&&x%2==0&&z%2==0;}
 public static boolean shell(int x,int y,int z,int width,int depth){return y==0||y==4||x==0||z==0||x==width-1||z==depth-1;}
 public static boolean socket(int x,int y,int z,int width,int depth){return(y==1||y==2)&&x>=2&&x<=width-3&&z>=2&&z<=depth-3&&x%2==0&&z%2==0;}
 public static Direction outward(Direction face,int x,int y,int z,int w,int d){return z==0?face:z==d-1?face.getOpposite():x==0?face.getCounterClockWise():face.getClockWise();}
 public static boolean corner(int x,int z,int w,int d){return (x==0||x==w-1)&&(z==0||z==d-1);}
 public static Direction cornerFacing(Direction face,int x,int z,int w,int d){return z==0?(x==0?face:face.getClockWise()):(x==0?face.getCounterClockWise():face.getOpposite());}
 private ArrayLayout(){}
}
