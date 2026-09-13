package FiveStage
import chisel3._
import chisel3.util.{ MuxLookup }
import chisel3.experimental.MultiIOModule

class Execute extends MultiIOModule {
    val io = IO(
        new Bundle {
            val op1 = Input(UInt(32.W))
            val op2 = Input(UInt(32.W))
            val aluOp = Input(UInt(4.W))
            val aluResult = Output(UInt(32.W))

            val branchType = Input(UInt(3.W))
            val branch = Input(Bool())
            val jump = Input(Bool())
            val immediate = Input(UInt(32.W))
            val PCout = Input(UInt(32.W))

            val branchTaken = Output(Bool())
            val branchTarget = Output(UInt(32.W))
        }
    )

    val alu = Module(new ALU).io
    alu.op1 := io.op1
    alu.op2 := io.op2
    alu.aluOp := io.aluOp
    val comparison = MuxLookup(io.branchType, false.B, Array(
        branchType.beq  -> (io.op1 === io.op2),
        branchType.neq  -> (io.op1 =/= io.op2),
        branchType.lt   -> (io.op1.asSInt < io.op2.asSInt),
        branchType.gte  -> (io.op1.asSInt >= io.op2.asSInt),
        branchType.ltu  -> (io.op1 < io.op2),
        branchType.gteu -> (io.op1 >= io.op2),
    ))

    io.branchTaken  := io.jump || (io.branch && comparison)
    io.branchTarget := Mux(io.branch, io.PCout + io.immediate, alu.aluResult)
    io.aluResult    := Mux(io.jump, io.PCout + 4.U, alu.aluResult)
}
