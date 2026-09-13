package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {
      val aluResult = Input(UInt(32.W))
      val rs2Data = Input(UInt(32.W))
      val memRead = Input(Bool())
      val memWrite = Input(Bool())
      val writeBackData = Output(UInt(32.W))
      val dataOut = Output(UInt(32.W))
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates



  io.writeBackData := io.aluResult
  
  DMEM.io.dataIn      := io.rs2Data
  DMEM.io.dataAddress := io.aluResult(11, 0)
  DMEM.io.writeEnable := io.memWrite
  io.dataOut := DMEM.io.dataOut
}
